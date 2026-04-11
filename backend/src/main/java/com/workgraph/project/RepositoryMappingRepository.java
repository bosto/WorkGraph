package com.workgraph.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryMappingRepository extends JpaRepository<RepositoryMapping, Long> {
    List<RepositoryMapping> findByProjectId(Long projectId);
    Optional<RepositoryMapping> findByGithubOwnerAndGithubRepo(String owner, String repo);
}
