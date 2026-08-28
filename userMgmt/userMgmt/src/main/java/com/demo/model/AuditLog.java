package com.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email", nullable = false, length = 150)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "target_entity", length = 50)
    private String targetEntity;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(Long actorId, String actorEmail, AuditAction action, String targetEntity,
                    Long targetId, String description, String ipAddress) {
        this();
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "AuditLog [id=" + id + ", actorId=" + actorId + ", actorEmail=" + actorEmail +
                ", action=" + action + ", targetEntity=" + targetEntity + ", targetId=" + targetId +
                ", description=" + description + ", timestamp=" + timestamp + ", ipAddress=" + ipAddress + "]";
    }
}