package com.workgraph.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByActiveTrue();
    Optional<Staff> findByEmployeeId(String employeeId);
    Optional<Staff> findByEmail(String email);
    List<Staff> findByTeam(String team);
}
