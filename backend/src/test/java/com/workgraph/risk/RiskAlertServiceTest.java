package com.workgraph.risk;

import com.workgraph.jira.JiraIssue;
import com.workgraph.jira.JiraIssueRepository;
import com.workgraph.mapping.AccountMapping;
import com.workgraph.mapping.AccountMappingRepository;
import com.workgraph.staff.Staff;
import com.workgraph.staff.StaffRepository;
import com.workgraph.workitem.WorkItem;
import com.workgraph.workitem.WorkItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAlertServiceTest {

    @Mock
    private RiskAlertRepository riskAlertRepository;
    @Mock
    private JiraIssueRepository jiraIssueRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private AccountMappingRepository accountMappingRepository;
    @Mock
    private WorkItemRepository workItemRepository;

    @InjectMocks
    private RiskAlertService riskAlertService;

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        staff.setId(1L);
        staff.setName("Bob Jones");
        staff.setEmail("bob@example.com");
        staff.setEmployeeId("EMP002");
        staff.setActive(true);
    }

    @Test
    void detectStaleTickets_createsAlerts() {
        JiraIssue issue = new JiraIssue();
        issue.setId(1L);
        issue.setKey("PROJ-1");
        issue.setSummary("Old ticket");
        issue.setStatus("In Progress");
        issue.setUpdatedAt(LocalDateTime.now().minusDays(10));

        when(jiraIssueRepository.findStaleIssues(any())).thenReturn(List.of(issue));
        when(riskAlertRepository.existsByAlertTypeAndTitleAndResolvedFalse("STALE_TICKET", "Stale ticket: PROJ-1")).thenReturn(false);
        when(riskAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        riskAlertService.detectStaleTickets();

        verify(riskAlertRepository).save(argThat(alert ->
            "STALE_TICKET".equals(alert.getAlertType()) &&
            alert.getTitle().contains("PROJ-1")
        ));
    }

    @Test
    void detectHighWip_createsAlert_whenAboveThreshold() {
        when(staffRepository.findByActiveTrue()).thenReturn(List.of(staff));

        WorkItem item1 = new WorkItem(); item1.setStatus("In Progress");
        WorkItem item2 = new WorkItem(); item2.setStatus("In Progress");
        WorkItem item3 = new WorkItem(); item3.setStatus("In Progress");
        WorkItem item4 = new WorkItem(); item4.setStatus("In Progress");
        WorkItem item5 = new WorkItem(); item5.setStatus("In Progress");

        when(workItemRepository.findByStaffId(1L)).thenReturn(List.of(item1, item2, item3, item4, item5));
        when(riskAlertRepository.findByStaffIdAndResolvedFalse(1L)).thenReturn(List.of());
        when(riskAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        riskAlertService.detectHighWip();

        verify(riskAlertRepository).save(argThat(alert ->
            "HIGH_WIP".equals(alert.getAlertType()) &&
            alert.getSeverity() == RiskAlert.Severity.HIGH
        ));
    }

    @Test
    void resolve_setsResolvedTrue() {
        RiskAlert alert = new RiskAlert();
        alert.setId(1L);
        alert.setAlertType("STALE_TICKET");
        alert.setSeverity(RiskAlert.Severity.MEDIUM);
        alert.setTitle("Test alert");
        alert.setResolved(false);

        when(riskAlertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(riskAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RiskAlert resolved = riskAlertService.resolve(1L);

        assertThat(resolved.isResolved()).isTrue();
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    @Test
    void findActiveAlerts_returnsUnresolved() {
        RiskAlert alert = new RiskAlert();
        alert.setAlertType("HIGH_WIP");
        alert.setSeverity(RiskAlert.Severity.HIGH);
        alert.setTitle("High WIP");
        when(riskAlertRepository.findByResolvedFalse()).thenReturn(List.of(alert));

        List<RiskAlert> alerts = riskAlertService.findActiveAlerts();
        assertThat(alerts).hasSize(1);
    }
}
