package com.workgraph.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubPullRequestRepository extends JpaRepository<GitHubPullRequest, Long> {
    Optional<GitHubPullRequest> findByGithubId(Long githubId);
    List<GitHubPullRequest> findByAuthorStaffId(Long staffId);
    List<GitHubPullRequest> findByProjectId(Long projectId);
    List<GitHubPullRequest> findByGithubOwnerAndGithubRepo(String owner, String repo);
    List<GitHubPullRequest> findByState(String state);
    Optional<GitHubPullRequest> findTopByOrderBySyncedAtDesc();
}
