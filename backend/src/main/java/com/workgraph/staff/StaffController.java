package com.workgraph.staff;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<Staff> listActive() {
        return staffService.findAllActive();
    }

    @GetMapping("/all")
    public List<Staff> listAll() {
        return staffService.findAll();
    }

    @GetMapping("/{id}")
    public Staff getById(@PathVariable Long id) {
        return staffService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Staff create(@Valid @RequestBody Staff staff) {
        return staffService.create(staff);
    }

    @PutMapping("/{id}")
    public Staff update(@PathVariable Long id, @Valid @RequestBody Staff staff) {
        return staffService.update(id, staff);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        staffService.deactivate(id);
    }
}
