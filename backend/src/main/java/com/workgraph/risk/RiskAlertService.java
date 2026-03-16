package com.workgraph.risk;

import com.workgraph.jira.JiraIssue;
import com.workgraph.jira.JiraIssueRepository;
import com.workgraph.mapping.AccountMapping;
import com.workgraph.mapping.AccountMappingRepository;
import com.workgraph.staff.Staff;
import com.workgraph.staff.StaffRepository;
import com.workgraph.workitem.WorkItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RiskAlertService {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertService.class);

    private static final int STALE_DAYS = 7;
    private static final int HIGH_WIP_THRESHOLD = 5;

    private final RiskAlertRepository riskAlertRepository;
    private final JiraIssueRepository jiraIssueRepository;
    private final StaffRepository staffRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final WorkItemRepository workItemRepository;

    public RiskAlertService(RiskAlertRepository riskAlertRepository,
                             JiraIssueRepository jiraIssueRepository,
                             StaffRepository staffRepository,
                             AccountMappingRepository accountMappingRepository,
                             WorkItemRepository workItemRepository) {
        this.riskAlertRepository = riskAlertRepository;
        this.jiraIssueRepository = jiraIssueRepository;
        this.staffRepository = staffRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.workItemRepository = workItemRepository;
    }

    public List<RiskAlert> findActiveAlerts() {
        return riskAlertRepository.findByResolvedFalse();
    }

    public List<RiskAlert> findByStaff(Long staffId) {
        return riskAlertRepository.findByStaffIdAndResolvedFalse(staffId);
    }

    public List<RiskAlert> findByProject(Long projectId) {
        return riskAlertRepository.findByProjectIdAndResolvedFalse(projectId);
    }

    public RiskAlert resolve(Long id) {
        RiskAlert alert = riskAlertRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException("Alert not found: " + id));
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        return riskAlertRepository.save(alert);
    }

    @Scheduled(cron = "${risk.scan-cron:0 0 * * * *}")
    public void runRiskScan() {
        log.info("Running risk alert scan");
        detectStaleTickets();
        detectUnmappedAccounts();
        detectHighWip();
    }

    public void detectStaleTickets() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusDays(STALE_DAYS);
        List<JiraIssue> staleIssues = jiraIssueRepository.findStaleIssues(staleThreshold);

        for (JiraIssue issue : staleIssues) {
            String alertType = "STALE_TICKET";
            String title = "Stale ticket: " + issue.getKey();
            if (!riskAlertRepository.existsByAlertTypeAndTitleAndResolvedFalse(alertType, title)) {
                RiskAlert alert = new RiskAlert();
                alert.setAlertType(alertType);
                alert.setSeverity(RiskAlert.Severity.MEDIUM);
                alert.setStaff(issue.getAssigneeStaff());
                alert.setProject(issue.getProject());
                alert.setTitle(title);
                alert.setDetail("Ticket " + issue.getKey() + " has not been updated in over " + STALE_DAYS + " days. Status: " + issue.getStatus());
                riskAlertRepository.save(alert);
            }
        }
    }

    public void detectUnmappedAccounts() {
        List<AccountMapping> unverified = accountMappingRepository.findByVerifiedFalse();
        for (AccountMapping mapping : unverified) {
            String alertType = "UNMAPPED_ACCOUNT";
            boolean exists = riskAlertRepository.findByAlertTypeAndResolvedFalse(alertType)
                .stream()
                .anyMatch(a -> a.getStaff() != null
                    && mapping.getStaff() != null
                    && mapping.getStaff().getId().equals(a.getStaff().getId()));

            if (!exists) {
                RiskAlert alert = new RiskAlert();
                alert.setAlertType(alertType);
                alert.setSeverity(RiskAlert.Severity.LOW);
                alert.setStaff(mapping.getStaff());
                alert.setTitle("Unverified account mapping for " + (mapping.getStaff() != null ? mapping.getStaff().getName() : "unknown"));
                alert.setDetail("Account " + mapping.getExternalUsername() + " (" + mapping.getAccountType() + ") is not yet verified.");
                riskAlertRepository.save(alert);
            }
        }
    }

    public void detectHighWip() {
        List<Staff> allStaff = staffRepository.findByActiveTrue();
        for (Staff staff : allStaff) {
            long activeItems = workItemRepository.findByStaffId(staff.getId()).stream()
                .filter(w -> w.getStatus() != null && !List.of("Done", "Resolved", "Closed", "merged", "closed").contains(w.getStatus()))
                .count();

            if (activeItems >= HIGH_WIP_THRESHOLD) {
                String alertType = "HIGH_WIP";
                boolean exists = riskAlertRepository.findByStaffIdAndResolvedFalse(staff.getId())
                    .stream()
                    .anyMatch(a -> alertType.equals(a.getAlertType()));

                if (!exists) {
                    RiskAlert alert = new RiskAlert();
                    alert.setAlertType(alertType);
                    alert.setSeverity(RiskAlert.Severity.HIGH);
                    alert.setStaff(staff);
                    alert.setTitle("High WIP for " + staff.getName());
                    alert.setDetail(staff.getName() + " has " + activeItems + " active work items (threshold: " + HIGH_WIP_THRESHOLD + ")");
                    riskAlertRepository.save(alert);
                }
            }
        }
    }
}
