package com.workgraph.workitem;

import com.workgraph.staff.Staff;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "work_event")
public class WorkEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_item_id")
    private WorkItem workItem;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public WorkEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public WorkItem getWorkItem() { return workItem; }
    public void setWorkItem(WorkItem workItem) { this.workItem = workItem; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getEventAt() { return eventAt; }
    public void setEventAt(LocalDateTime eventAt) { this.eventAt = eventAt; }
    public Map<String, Object> getDetail() { return detail; }
    public void setDetail(Map<String, Object> detail) { this.detail = detail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
