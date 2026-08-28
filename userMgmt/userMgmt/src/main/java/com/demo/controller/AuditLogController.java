package com.demo.controller;

import com.demo.model.AuditAction;
import com.demo.model.AuditLog;
import com.demo.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> logs = auditLogService.getAuditLogs(actorId, action, start, end, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", auditLogService.getTotalAuditLogs());
        stats.put("logsLast24Hours", auditLogService.getAuditLogsCountSince(LocalDateTime.now().minusDays(1)));
        stats.put("logsLast7Days", auditLogService.getAuditLogsCountSince(LocalDateTime.now().minusDays(7)));
        stats.put("failedLogins", auditLogService.getAuditLogsCountByAction(AuditAction.FAILED_LOGIN));
        stats.put("employeeCreations", auditLogService.getAuditLogsCountByAction(AuditAction.CREATE_EMPLOYEE));
        stats.put("employeeUpdates", auditLogService.getAuditLogsCountByAction(AuditAction.UPDATE_EMPLOYEE));
        stats.put("employeeTerminations", auditLogService.getAuditLogsCountByAction(AuditAction.TERMINATE_EMPLOYEE));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/actions")
    public ResponseEntity<AuditAction[]> getAuditActions() {
        return ResponseEntity.ok(AuditAction.values());
    }
}