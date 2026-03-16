package com.workgraph.github;

import com.workgraph.mapping.AccountMapping;
import com.workgraph.mapping.AccountMappingRepository;
import com.workgraph.project.Project;
import com.workgraph.project.RepositoryMapping;
import com.workgraph.project.RepositoryMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GitHubSyncService {

    private static final Logger log = LoggerFactory.getLogger(GitHubSyncService.class);

    private final GitHubCommitRepository commitRepository;
    private final GitHubPullRequestRepository prRepository;
    private final GitHubReviewRepository reviewRepository;
    private final RepositoryMappingRepository repoMappingRepository;
    private final AccountMappingRepository accountMappingRepository;

    @Value("${github.token:}")
    private String githubToken;

    public GitHubSyncService(GitHubCommitRepository commitRepository,
                              GitHubPullRequestRepository prRepository,
                              GitHubReviewRepository reviewRepository,
                              RepositoryMappingRepository repoMappingRepository,
                              AccountMappingRepository accountMappingRepository) {
        this.commitRepository = commitRepository;
        this.prRepository = prRepository;
        this.reviewRepository = reviewRepository;
        this.repoMappingRepository = repoMappingRepository;
        this.accountMappingRepository = accountMappingRepository;
    }

    public SyncStatus getStatus() {
        long commits = commitRepository.count();
        long prs = prRepository.count();
        LocalDateTime lastSync = prRepository.findTopByOrderBySyncedAtDesc()
            .map(GitHubPullRequest::getSyncedAt)
            .orElse(null);
        return new SyncStatus("GITHUB", commits, prs, lastSync);
    }

    @Scheduled(fixedDelayString = "${github.sync-interval-ms:3600000}")
    public void scheduledSync() {
        if (githubToken == null || githubToken.isBlank()) {
            log.debug("GitHub not configured, skipping sync");
            return;
        }
        log.info("Starting scheduled GitHub sync");
        syncAllRepositories();
    }

    public void syncAllRepositories() {
        List<RepositoryMapping> repos = repoMappingRepository.findAll();
        for (RepositoryMapping repo : repos) {
            try {
                syncRepository(repo);
            } catch (Exception e) {
                log.error("Failed to sync GitHub repo {}/{}: {}",
                    repo.getGithubOwner(), repo.getGithubRepo(), e.getMessage());
            }
        }
    }

    public void syncRepository(RepositoryMapping repoMapping) {
        if (githubToken == null || githubToken.isBlank()) {
            log.warn("GitHub token not configured");
            return;
        }

        String owner = repoMapping.getGithubOwner();
        String repo = repoMapping.getGithubRepo();
        Project project = repoMapping.getProject();

        log.info("Syncing GitHub repo: {}/{}", owner, repo);

        RestClient client = RestClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeaders(h -> {
                h.setBearerAuth(githubToken);
                h.set("Accept", "application/vnd.github+json");
                h.set("X-GitHub-Api-Version", "2022-11-28");
            })
            .build();

        syncCommits(client, owner, repo, project);
        syncPullRequests(client, owner, repo, project);
    }

    @SuppressWarnings("unchecked")
    private void syncCommits(RestClient client, String owner, String repo, Project project) {
        int page = 1;
        boolean hasMore = true;
        while (hasMore) {
            try {
                int currentPage = page;
                List<Map<String, Object>> commits = client.get()
                    .uri("/repos/{owner}/{repo}/commits?per_page=100&page={page}", owner, repo, currentPage)
                    .retrieve()
                    .body(List.class);

                if (commits == null || commits.isEmpty()) break;

                for (Map<String, Object> commitData : commits) {
                    String sha = (String) commitData.get("sha");
                    if (sha == null) continue;

                    GitHubCommit commit = commitRepository.findBySha(sha).orElse(new GitHubCommit());
                    commit.setSha(sha);
                    commit.setGithubOwner(owner);
                    commit.setGithubRepo(repo);
                    commit.setProject(project);

                    Map<String, Object> commitDetail = (Map<String, Object>) commitData.get("commit");
                    if (commitDetail != null) {
                        commit.setMessage((String) commitDetail.get("message"));
                        Map<String, Object> authorData = (Map<String, Object>) commitDetail.get("author");
                        if (authorData != null) {
                            String dateStr = (String) authorData.get("date");
                            if (dateStr != null) {
                                commit.setAuthoredAt(OffsetDateTime.parse(dateStr).toLocalDateTime());
                            }
                        }
                    }

                    Map<String, Object> authorUser = (Map<String, Object>) commitData.get("author");
                    if (authorUser != null) {
                        String login = (String) authorUser.get("login");
                        if (login != null) {
                            accountMappingRepository
                                .findByAccountTypeAndExternalAccountId(AccountMapping.AccountType.GITHUB, login)
                                .ifPresent(m -> commit.setAuthorStaff(m.getStaff()));
                        }
                    }

                    commit.setSyncedAt(LocalDateTime.now());
                    commitRepository.save(commit);
                }

                hasMore = commits.size() == 100;
                page++;
            } catch (Exception e) {
                log.error("Error syncing commits for {}/{}: {}", owner, repo, e.getMessage());
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void syncPullRequests(RestClient client, String owner, String repo, Project project) {
        int page = 1;
        boolean hasMore = true;
        while (hasMore) {
            try {
                int currentPage = page;
                List<Map<String, Object>> prs = client.get()
                    .uri("/repos/{owner}/{repo}/pulls?state=all&per_page=100&page={page}", owner, repo, currentPage)
                    .retrieve()
                    .body(List.class);

                if (prs == null || prs.isEmpty()) break;

                for (Map<String, Object> prData : prs) {
                    Number githubIdNum = (Number) prData.get("id");
                    if (githubIdNum == null) continue;
                    Long githubId = githubIdNum.longValue();

                    GitHubPullRequest pr = prRepository.findByGithubId(githubId).orElse(new GitHubPullRequest());
                    pr.setGithubId(githubId);
                    pr.setNumber(((Number) prData.get("number")).intValue());
                    pr.setGithubOwner(owner);
                    pr.setGithubRepo(repo);
                    pr.setProject(project);
                    pr.setTitle((String) prData.get("title"));
                    pr.setState((String) prData.get("state"));
                    pr.setDraft(Boolean.TRUE.equals(prData.get("draft")));

                    String createdAt = (String) prData.get("created_at");
                    if (createdAt != null) pr.setCreatedAt(OffsetDateTime.parse(createdAt).toLocalDateTime());
                    String updatedAt = (String) prData.get("updated_at");
                    if (updatedAt != null) pr.setUpdatedAt(OffsetDateTime.parse(updatedAt).toLocalDateTime());
                    String mergedAt = (String) prData.get("merged_at");
                    if (mergedAt != null) pr.setMergedAt(OffsetDateTime.parse(mergedAt).toLocalDateTime());
                    String closedAt = (String) prData.get("closed_at");
                    if (closedAt != null) pr.setClosedAt(OffsetDateTime.parse(closedAt).toLocalDateTime());

                    Map<String, Object> user = (Map<String, Object>) prData.get("user");
                    if (user != null) {
                        String login = (String) user.get("login");
                        if (login != null) {
                            accountMappingRepository
                                .findByAccountTypeAndExternalAccountId(AccountMapping.AccountType.GITHUB, login)
                                .ifPresent(m -> pr.setAuthorStaff(m.getStaff()));
                        }
                    }

                    pr.setSyncedAt(LocalDateTime.now());
                    GitHubPullRequest savedPr = prRepository.save(pr);

                    syncReviews(client, owner, repo, savedPr);
                }

                hasMore = prs.size() == 100;
                page++;
            } catch (Exception e) {
                log.error("Error syncing PRs for {}/{}: {}", owner, repo, e.getMessage());
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void syncReviews(RestClient client, String owner, String repo, GitHubPullRequest pr) {
        try {
            List<Map<String, Object>> reviews = client.get()
                .uri("/repos/{owner}/{repo}/pulls/{number}/reviews", owner, repo, pr.getNumber())
                .retrieve()
                .body(List.class);

            if (reviews == null) return;

            for (Map<String, Object> reviewData : reviews) {
                Number githubIdNum = (Number) reviewData.get("id");
                if (githubIdNum == null) continue;
                Long githubId = githubIdNum.longValue();

                GitHubReview review = reviewRepository.findByGithubId(githubId).orElse(new GitHubReview());
                review.setGithubId(githubId);
                review.setPullRequest(pr);
                review.setState((String) reviewData.get("state"));

                String submittedAt = (String) reviewData.get("submitted_at");
                if (submittedAt != null) review.setSubmittedAt(OffsetDateTime.parse(submittedAt).toLocalDateTime());

                Map<String, Object> user = (Map<String, Object>) reviewData.get("user");
                if (user != null) {
                    String login = (String) user.get("login");
                    if (login != null) {
                        accountMappingRepository
                            .findByAccountTypeAndExternalAccountId(AccountMapping.AccountType.GITHUB, login)
                            .ifPresent(m -> review.setReviewerStaff(m.getStaff()));
                    }
                }

                review.setSyncedAt(LocalDateTime.now());
                reviewRepository.save(review);
            }
        } catch (Exception e) {
            log.error("Error syncing reviews for PR #{}: {}", pr.getNumber(), e.getMessage());
        }
    }

    public record SyncStatus(String source, long commitCount, long prCount, LocalDateTime lastSyncAt) {}
}
