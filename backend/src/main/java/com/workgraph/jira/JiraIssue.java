package com.workgraph.jira;

import com.workgraph.project.Project;
import com.workgraph.staff.Staff;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jira_issue")
public class JiraIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jira_id", unique = true, nullable = false)
    private String jiraId;

    @Column(name = "key", nullable = false)
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_staff_id")
    private Staff assigneeStaff;

    @Column(nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "issue_type")
    private String issueType;

    private String status;
    private String priority;

    @Column(name = "story_points", precision = 5, scale = 1)
    private BigDecimal storyPoints;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt = LocalDateTime.now();

    public JiraIssue() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJiraId() { return jiraId; }
    public void setJiraId(String jiraId) { this.jiraId = jiraId; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Staff getAssigneeStaff() { return assigneeStaff; }
    public void setAssigneeStaff(Staff assigneeStaff) { this.assigneeStaff = assigneeStaff; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public BigDecimal getStoryPoints() { return storyPoints; }
    public void setStoryPoints(BigDecimal storyPoints) { this.storyPoints = storyPoints; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
