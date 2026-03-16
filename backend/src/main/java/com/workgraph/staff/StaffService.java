package com.workgraph.staff;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Cacheable("staff-all")
    public List<Staff> findAllActive() {
        return staffRepository.findByActiveTrue();
    }

    public Staff findById(Long id) {
        return staffRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Staff not found: " + id));
    }

    public Staff findByEmployeeId(String employeeId) {
        return staffRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new NoSuchElementException("Staff not found: " + employeeId));
    }

    @CacheEvict(value = "staff-all", allEntries = true)
    public Staff create(Staff staff) {
        return staffRepository.save(staff);
    }

    @CacheEvict(value = "staff-all", allEntries = true)
    public Staff update(Long id, Staff updates) {
        Staff staff = findById(id);
        staff.setName(updates.getName());
        staff.setEmail(updates.getEmail());
        staff.setTeam(updates.getTeam());
        staff.setRole(updates.getRole());
        staff.setActive(updates.isActive());
        return staffRepository.save(staff);
    }

    @CacheEvict(value = "staff-all", allEntries = true)
    public void deactivate(Long id) {
        Staff staff = findById(id);
        staff.setActive(false);
        staffRepository.save(staff);
    }

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }
}
