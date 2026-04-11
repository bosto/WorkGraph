package com.workgraph.project;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final RepositoryMappingRepository repoMappingRepository;

    public ProjectService(ProjectRepository projectRepository,
                          RepositoryMappingRepository repoMappingRepository) {
        this.projectRepository = projectRepository;
        this.repoMappingRepository = repoMappingRepository;
    }

    @Cacheable("projects-active")
    public List<Project> findAllActive() {
        return projectRepository.findByActiveTrue();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Project not found: " + id));
    }

    public Project findByKey(String key) {
        return projectRepository.findByKey(key)
            .orElseThrow(() -> new NoSuchElementException("Project not found: " + key));
    }

    @CacheEvict(value = "projects-active", allEntries = true)
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    @CacheEvict(value = "projects-active", allEntries = true)
    public Project update(Long id, Project updates) {
        Project project = findById(id);
        project.setName(updates.getName());
        project.setDescription(updates.getDescription());
        project.setJiraProjectKey(updates.getJiraProjectKey());
        project.setActive(updates.isActive());
        return projectRepository.save(project);
    }

    public RepositoryMapping addRepoMapping(Long projectId, RepositoryMapping mapping) {
        Project project = findById(projectId);
        mapping.setProject(project);
        return repoMappingRepository.save(mapping);
    }

    public List<RepositoryMapping> getRepoMappings(Long projectId) {
        return repoMappingRepository.findByProjectId(projectId);
    }

    public void removeRepoMapping(Long mappingId) {
        repoMappingRepository.deleteById(mappingId);
    }
}
