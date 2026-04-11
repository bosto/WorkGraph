package com.workgraph.risk;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    public RiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping
    public List<RiskAlert> listActive(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long projectId) {
        if (staffId != null) return riskAlertService.findByStaff(staffId);
        if (projectId != null) return riskAlertService.findByProject(projectId);
        return riskAlertService.findActiveAlerts();
    }

    @PostMapping("/{id}/resolve")
    public RiskAlert resolve(@PathVariable Long id) {
        return riskAlertService.resolve(id);
    }

    @PostMapping("/scan")
    public String triggerScan() {
        riskAlertService.runRiskScan();
        return "Risk scan triggered";
    }
}
