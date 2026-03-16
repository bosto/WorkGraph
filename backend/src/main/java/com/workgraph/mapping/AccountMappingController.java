package com.workgraph.mapping;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mappings")
public class AccountMappingController {

    private final AccountMappingService service;

    public AccountMappingController(AccountMappingService service) {
        this.service = service;
    }

    @GetMapping
    public List<AccountMapping> listAll() {
        return service.findAll();
    }

    @GetMapping("/staff/{staffId}")
    public List<AccountMapping> listByStaff(@PathVariable Long staffId) {
        return service.findByStaff(staffId);
    }

    @GetMapping("/unverified")
    public List<AccountMapping> listUnverified() {
        return service.findUnverified();
    }

    @PostMapping("/staff/{staffId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountMapping create(@PathVariable Long staffId, @Valid @RequestBody AccountMapping mapping) {
        return service.create(staffId, mapping);
    }

    @PostMapping("/{id}/verify")
    public AccountMapping verify(@PathVariable Long id) {
        return service.verify(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
