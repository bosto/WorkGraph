package com.workgraph.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffService staffService;

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        staff.setId(1L);
        staff.setEmployeeId("EMP001");
        staff.setName("Alice Smith");
        staff.setEmail("alice@example.com");
        staff.setTeam("Engineering");
        staff.setRole("Senior Engineer");
        staff.setActive(true);
    }

    @Test
    void findById_returnsStaff() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        Staff found = staffService.findById(1L);
        assertThat(found.getName()).isEqualTo("Alice Smith");
    }

    @Test
    void findById_notFound_throws() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> staffService.findById(99L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_savesAndReturns() {
        when(staffRepository.save(any())).thenReturn(staff);
        Staff created = staffService.create(staff);
        assertThat(created.getEmployeeId()).isEqualTo("EMP001");
        verify(staffRepository).save(staff);
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any())).thenReturn(staff);
        staffService.deactivate(1L);
        assertThat(staff.isActive()).isFalse();
    }

    @Test
    void findAllActive_returnsList() {
        when(staffRepository.findByActiveTrue()).thenReturn(List.of(staff));
        List<Staff> active = staffService.findAllActive();
        assertThat(active).hasSize(1);
    }
}
