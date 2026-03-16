package com.workgraph.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByActiveTrue();
    Optional<Project> findByKey(String key);
    Optional<Project> findByJiraProjectKey(String jiraProjectKey);
}
