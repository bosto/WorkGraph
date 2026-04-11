package com.workgraph.mapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountMappingRepository extends JpaRepository<AccountMapping, Long> {
    List<AccountMapping> findByStaffId(Long staffId);
    List<AccountMapping> findByAccountType(AccountMapping.AccountType accountType);
    Optional<AccountMapping> findByAccountTypeAndExternalAccountId(
        AccountMapping.AccountType accountType, String externalAccountId);
    List<AccountMapping> findByVerifiedFalse();
}
