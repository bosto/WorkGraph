package com.workgraph.github;

import com.workgraph.staff.Staff;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_review")
public class GitHubReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", unique = true, nullable = false)
    private Long githubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private GitHubPullRequest pullRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_staff_id")
    private Staff reviewerStaff;

    @Column(nullable = false)
    private String state;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt = LocalDateTime.now();

    public GitHubReview() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGithubId() { return githubId; }
    public void setGithubId(Long githubId) { this.githubId = githubId; }
    public GitHubPullRequest getPullRequest() { return pullRequest; }
    public void setPullRequest(GitHubPullRequest pullRequest) { this.pullRequest = pullRequest; }
    public Staff getReviewerStaff() { return reviewerStaff; }
    public void setReviewerStaff(Staff reviewerStaff) { this.reviewerStaff = reviewerStaff; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
