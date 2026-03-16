package com.workgraph.github;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubReviewRepository extends JpaRepository<GitHubReview, Long> {
    List<GitHubReview> findByReviewerStaffId(Long staffId);
    List<GitHubReview> findByPullRequestId(Long prId);
    Optional<GitHubReview> findByGithubId(Long githubId);
}
