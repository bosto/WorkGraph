package com.workgraph.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubCommitRepository extends JpaRepository<GitHubCommit, Long> {
    Optional<GitHubCommit> findBySha(String sha);
    List<GitHubCommit> findByAuthorStaffId(Long staffId);
    List<GitHubCommit> findByProjectId(Long projectId);
    List<GitHubCommit> findByGithubOwnerAndGithubRepo(String owner, String repo);
}
