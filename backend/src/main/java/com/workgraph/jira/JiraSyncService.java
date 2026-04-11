package com.workgraph.jira;

import com.workgraph.mapping.AccountMapping;
import com.workgraph.mapping.AccountMappingRepository;
import com.workgraph.project.Project;
import com.workgraph.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class JiraSyncService {

    private static final Logger log = LoggerFactory.getLogger(JiraSyncService.class);

    private final JiraIssueRepository jiraIssueRepository;
    private final ProjectRepository projectRepository;
    private final AccountMappingRepository accountMappingRepository;

    @Value("${jira.base-url:}")
    private String jiraBaseUrl;

    @Value("${jira.email:}")
    private String jiraEmail;

    @Value("${jira.token:}")
    private String jiraToken;

    public JiraSyncService(JiraIssueRepository jiraIssueRepository,
                           ProjectRepository projectRepository,
                           AccountMappingRepository accountMappingRepository) {
        this.jiraIssueRepository = jiraIssueRepository;
        this.projectRepository = projectRepository;
        this.accountMappingRepository = accountMappingRepository;
    }

    public SyncStatus getStatus() {
        long total = jiraIssueRepository.count();
        LocalDateTime lastSync = jiraIssueRepository.findTopByOrderBySyncedAtDesc()
            .map(JiraIssue::getSyncedAt)
            .orElse(null);
        return new SyncStatus("JIRA", total, lastSync);
    }

    @Scheduled(fixedDelayString = "${jira.sync-interval-ms:3600000}")
    public void scheduledSync() {
        if (jiraBaseUrl == null || jiraBaseUrl.isBlank()) {
            log.debug("Jira not configured, skipping sync");
            return;
        }
        log.info("Starting scheduled Jira sync");
        syncAllProjects();
    }

    public void syncAllProjects() {
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            if (project.getJiraProjectKey() != null) {
                try {
                    syncProject(project);
                } catch (Exception e) {
                    log.error("Failed to sync Jira project {}: {}", project.getJiraProjectKey(), e.getMessage());
                }
            }
        }
    }

    public void syncProject(Project project) {
        if (jiraBaseUrl == null || jiraBaseUrl.isBlank()) {
            log.warn("Jira not configured");
            return;
        }
        log.info("Syncing Jira project: {}", project.getJiraProjectKey());

        RestClient client = RestClient.builder()
            .baseUrl(jiraBaseUrl)
            .defaultHeaders(h -> {
                h.setBasicAuth(jiraEmail, jiraToken);
                h.set("Accept", "application/json");
            })
            .build();

        String jql = "project=" + project.getJiraProjectKey() + " ORDER BY updated DESC";
        int startAt = 0;
        int maxResults = 100;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String uri = UriComponentsBuilder.fromPath("/rest/api/3/search")
                    .queryParam("jql", jql)
                    .queryParam("startAt", startAt)
                    .queryParam("maxResults", maxResults)
                    .build()
                    .toUriString();
                @SuppressWarnings("unchecked")
                Map<String, Object> response = client.get().uri(uri)
                    .retrieve().body(Map.class);

                if (response == null) break;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.get("issues");
                if (issues == null || issues.isEmpty()) break;

                for (Map<String, Object> issueData : issues) {
                    upsertIssue(issueData, project);
                }

                int total = (Integer) response.getOrDefault("total", 0);
                startAt += issues.size();
                hasMore = startAt < total;
            } catch (Exception e) {
                log.error("Error fetching Jira issues: {}", e.getMessage());
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void upsertIssue(Map<String, Object> data, Project project) {
        String jiraId = (String) data.get("id");
        String key = (String) data.get("key");
        Map<String, Object> fields = (Map<String, Object>) data.get("fields");
        if (fields == null) return;

        JiraIssue issue = jiraIssueRepository.findByJiraId(jiraId)
            .orElse(new JiraIssue());

        issue.setJiraId(jiraId);
        issue.setKey(key);
        issue.setProject(project);

        String summary = (String) fields.get("summary");
        issue.setSummary(summary != null ? summary : "");

        Map<String, Object> statusField = (Map<String, Object>) fields.get("status");
        if (statusField != null) {
            issue.setStatus((String) statusField.get("name"));
        }

        Map<String, Object> issueTypeField = (Map<String, Object>) fields.get("issuetype");
        if (issueTypeField != null) {
            issue.setIssueType((String) issueTypeField.get("name"));
        }

        Map<String, Object> priorityField = (Map<String, Object>) fields.get("priority");
        if (priorityField != null) {
            issue.setPriority((String) priorityField.get("name"));
        }

        Map<String, Object> assigneeField = (Map<String, Object>) fields.get("assignee");
        if (assigneeField != null) {
            String accountId = (String) assigneeField.get("accountId");
            if (accountId != null) {
                Optional<AccountMapping> mapping = accountMappingRepository
                    .findByAccountTypeAndExternalAccountId(AccountMapping.AccountType.JIRA, accountId);
                mapping.ifPresent(m -> issue.setAssigneeStaff(m.getStaff()));
            }
        }

        issue.setSyncedAt(LocalDateTime.now());
        jiraIssueRepository.save(issue);
    }

    public record SyncStatus(String source, long issueCount, LocalDateTime lastSyncAt) {}
}
