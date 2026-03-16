package com.workgraph.mapping;

import com.workgraph.staff.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_mapping",
    uniqueConstraints = @UniqueConstraint(columnNames = {"account_type", "external_account_id"}))
public class AccountMapping {

    public enum AccountType { JIRA, GITHUB }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    @NotNull
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @NotNull
    private AccountType accountType;

    @Column(name = "external_account_id", nullable = false)
    @NotBlank
    private String externalAccountId;

    @Column(name = "external_username")
    private String externalUsername;

    private boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AccountMapping() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public String getExternalAccountId() { return externalAccountId; }
    public void setExternalAccountId(String externalAccountId) { this.externalAccountId = externalAccountId; }
    public String getExternalUsername() { return externalUsername; }
    public void setExternalUsername(String externalUsername) { this.externalUsername = externalUsername; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
