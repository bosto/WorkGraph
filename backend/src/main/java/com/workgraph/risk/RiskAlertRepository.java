package com.workgraph.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findByResolvedFalse();
    List<RiskAlert> findByStaffIdAndResolvedFalse(Long staffId);
    List<RiskAlert> findByProjectIdAndResolvedFalse(Long projectId);
    List<RiskAlert> findBySeverityAndResolvedFalse(RiskAlert.Severity severity);
    List<RiskAlert> findByAlertTypeAndResolvedFalse(String alertType);
    boolean existsByAlertTypeAndTitleAndResolvedFalse(String alertType, String title);
}
