package com.workgraph.github;

import com.workgraph.project.Project;
import com.workgraph.staff.Staff;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_commit")
public class GitHubCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 40)
    private String sha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_staff_id")
    private Staff authorStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "github_owner", nullable = false)
    private String githubOwner;

    @Column(name = "github_repo", nullable = false)
    private String githubRepo;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "authored_at")
    private LocalDateTime authoredAt;

    private int additions;
    private int deletions;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt = LocalDateTime.now();

    public GitHubCommit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSha() { return sha; }
    public void setSha(String sha) { this.sha = sha; }
    public Staff getAuthorStaff() { return authorStaff; }
    public void setAuthorStaff(Staff authorStaff) { this.authorStaff = authorStaff; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getGithubOwner() { return githubOwner; }
    public void setGithubOwner(String githubOwner) { this.githubOwner = githubOwner; }
    public String getGithubRepo() { return githubRepo; }
    public void setGithubRepo(String githubRepo) { this.githubRepo = githubRepo; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getAuthoredAt() { return authoredAt; }
    public void setAuthoredAt(LocalDateTime authoredAt) { this.authoredAt = authoredAt; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
