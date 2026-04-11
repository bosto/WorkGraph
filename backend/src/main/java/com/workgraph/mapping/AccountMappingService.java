package com.workgraph.mapping;

import com.workgraph.staff.Staff;
import com.workgraph.staff.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class AccountMappingService {

    private final AccountMappingRepository mappingRepository;
    private final StaffRepository staffRepository;

    public AccountMappingService(AccountMappingRepository mappingRepository,
                                  StaffRepository staffRepository) {
        this.mappingRepository = mappingRepository;
        this.staffRepository = staffRepository;
    }

    public List<AccountMapping> findAll() {
        return mappingRepository.findAll();
    }

    public List<AccountMapping> findByStaff(Long staffId) {
        return mappingRepository.findByStaffId(staffId);
    }

    public List<AccountMapping> findUnverified() {
        return mappingRepository.findByVerifiedFalse();
    }

    public Optional<AccountMapping> findByExternalId(AccountMapping.AccountType type, String externalId) {
        return mappingRepository.findByAccountTypeAndExternalAccountId(type, externalId);
    }

    public AccountMapping create(Long staffId, AccountMapping mapping) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new NoSuchElementException("Staff not found: " + staffId));
        mapping.setStaff(staff);
        return mappingRepository.save(mapping);
    }

    public AccountMapping verify(Long id) {
        AccountMapping mapping = mappingRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Mapping not found: " + id));
        mapping.setVerified(true);
        return mappingRepository.save(mapping);
    }

    public void delete(Long id) {
        mappingRepository.deleteById(id);
    }
}
