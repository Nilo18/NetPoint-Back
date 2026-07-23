package com.netpoint.main.dto;

import com.netpoint.main.models.AuditLog;

public record EventTypeCountDTO(
        AuditLog.EventType eventType,
        Long count
) {
}
