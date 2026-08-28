package com.demo.service;

import com.demo.model.AuditAction;
import com.demo.model.AuditLog;
import com.demo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog logAction(Long actorId, String actorEmail, AuditAction action,
                               String targetEntity, Long targetId, String description, String ipAddress) {
        AuditLog auditLog = new AuditLog(actorId, actorEmail, action, targetEntity, targetId, description, ipAddress);
        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(Long actorId, AuditAction action,
                                        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findWithFilters(actorId, action, startDate, endDate, pageable);
    }

    public List<AuditLog> getAuditLogsByActor(Long actorId) {
        return auditLogRepository.findByActorIdOrderByTimestampDesc(actorId);
    }

    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public long getTotalAuditLogs() {
        return auditLogRepository.count();
    }

    public long getAuditLogsCountByAction(AuditAction action) {
        return auditLogRepository.countByAction(action);
    }

    public long getAuditLogsCountSince(LocalDateTime date) {
        return auditLogRepository.countByTimestampAfter(date);
    }
}