package com.workgraph.workitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkEventRepository extends JpaRepository<WorkEvent, Long> {
    List<WorkEvent> findByStaffId(Long staffId);
    List<WorkEvent> findByWorkItemId(Long workItemId);
    List<WorkEvent> findByStaffIdAndEventAtBetween(Long staffId, LocalDateTime from, LocalDateTime to);
}
