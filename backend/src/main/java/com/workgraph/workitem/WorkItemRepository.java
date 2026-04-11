package com.workgraph.workitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    List<WorkItem> findByStaffId(Long staffId);
    List<WorkItem> findByProjectId(Long projectId);
    List<WorkItem> findByStaffIdAndStatus(Long staffId, String status);
}
