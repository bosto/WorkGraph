package com.workgraph.github;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubSyncController {

    private final GitHubSyncService syncService;
    private final GitHubCommitRepository commitRepository;
    private final GitHubPullRequestRepository prRepository;

    public GitHubSyncController(GitHubSyncService syncService,
                                 GitHubCommitRepository commitRepository,
                                 GitHubPullRequestRepository prRepository) {
        this.syncService = syncService;
        this.commitRepository = commitRepository;
        this.prRepository = prRepository;
    }

    @GetMapping("/commits")
    public List<GitHubCommit> listCommits(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long projectId) {
        if (staffId != null) return commitRepository.findByAuthorStaffId(staffId);
        if (projectId != null) return commitRepository.findByProjectId(projectId);
        return commitRepository.findAll();
    }

    @GetMapping("/prs")
    public List<GitHubPullRequest> listPRs(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long projectId) {
        if (staffId != null) return prRepository.findByAuthorStaffId(staffId);
        if (projectId != null) return prRepository.findByProjectId(projectId);
        return prRepository.findAll();
    }

    @GetMapping("/status")
    public GitHubSyncService.SyncStatus getStatus() {
        return syncService.getStatus();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        syncService.syncAllRepositories();
        return ResponseEntity.ok("Sync triggered");
    }
}
