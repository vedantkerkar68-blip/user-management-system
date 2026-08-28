package com.demo.repository;

import com.demo.model.AuditLog;
import com.demo.model.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorIdOrderByTimestampDesc(Long actorId);

    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:actorId IS NULL OR a.actorId = :actorId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(
            @Param("actorId") Long actorId,
            @Param("action") AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByAction(AuditAction action);

    long countByTimestampAfter(LocalDateTime date);
}