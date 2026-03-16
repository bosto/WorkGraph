package com.workgraph.jira;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JiraIssueRepository extends JpaRepository<JiraIssue, Long> {
    Optional<JiraIssue> findByJiraId(String jiraId);
    List<JiraIssue> findByAssigneeStaffId(Long staffId);
    List<JiraIssue> findByProjectId(Long projectId);
    List<JiraIssue> findByStatus(String status);

    @Query("SELECT j FROM JiraIssue j WHERE j.status NOT IN ('Done', 'Resolved', 'Closed') AND j.updatedAt < :staleThreshold")
    List<JiraIssue> findStaleIssues(LocalDateTime staleThreshold);

    @Query("SELECT j FROM JiraIssue j WHERE j.assigneeStaff IS NULL AND j.status NOT IN ('Done', 'Resolved', 'Closed')")
    List<JiraIssue> findUnassignedActiveIssues();
}
