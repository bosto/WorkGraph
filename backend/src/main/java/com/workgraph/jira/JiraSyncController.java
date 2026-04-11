package com.workgraph.jira;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jira")
public class JiraSyncController {

    private final JiraSyncService syncService;
    private final JiraIssueRepository issueRepository;

    public JiraSyncController(JiraSyncService syncService, JiraIssueRepository issueRepository) {
        this.syncService = syncService;
        this.issueRepository = issueRepository;
    }

    @GetMapping("/issues")
    public List<JiraIssue> listIssues(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long projectId) {
        if (staffId != null) return issueRepository.findByAssigneeStaffId(staffId);
        if (projectId != null) return issueRepository.findByProjectId(projectId);
        return issueRepository.findAll();
    }

    @GetMapping("/status")
    public JiraSyncService.SyncStatus getStatus() {
        return syncService.getStatus();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        syncService.syncAllProjects();
        return ResponseEntity.ok("Sync triggered");
    }
}
