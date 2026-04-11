package com.workgraph.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> listActive() {
        return projectService.findAllActive();
    }

    @GetMapping("/{id}")
    public Project getById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project create(@Valid @RequestBody Project project) {
        return projectService.create(project);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @Valid @RequestBody Project project) {
        return projectService.update(id, project);
    }

    @GetMapping("/{id}/repos")
    public List<RepositoryMapping> getRepos(@PathVariable Long id) {
        return projectService.getRepoMappings(id);
    }

    @PostMapping("/{id}/repos")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryMapping addRepo(@PathVariable Long id,
                                      @Valid @RequestBody RepositoryMapping mapping) {
        return projectService.addRepoMapping(id, mapping);
    }

    @DeleteMapping("/repos/{mappingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRepo(@PathVariable Long mappingId) {
        projectService.removeRepoMapping(mappingId);
    }
}
