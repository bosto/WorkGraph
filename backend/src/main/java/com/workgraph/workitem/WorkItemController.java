package com.workgraph.workitem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

    private final WorkItemRepository workItemRepository;
    private final WorkEventRepository workEventRepository;

    public WorkItemController(WorkItemRepository workItemRepository,
                               WorkEventRepository workEventRepository) {
        this.workItemRepository = workItemRepository;
        this.workEventRepository = workEventRepository;
    }

    @GetMapping
    public List<WorkItem> list(@RequestParam(required = false) Long staffId,
                                @RequestParam(required = false) Long projectId) {
        if (staffId != null) return workItemRepository.findByStaffId(staffId);
        if (projectId != null) return workItemRepository.findByProjectId(projectId);
        return workItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public WorkItem getById(@PathVariable Long id) {
        return workItemRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkItem not found: " + id));
    }

    @GetMapping("/{id}/events")
    public List<WorkEvent> getEvents(@PathVariable Long id) {
        return workEventRepository.findByWorkItemId(id);
    }
}
