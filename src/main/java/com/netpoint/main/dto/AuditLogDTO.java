package com.netpoint.main.dto;

import com.netpoint.main.models.AuditLog;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Integer id,
        AuditLog.EventType eventType,
        String details,
        String actorNameSnapshot,
        String actorRoleSnapshot,
        LocalDateTime occurredAt
) {
}
