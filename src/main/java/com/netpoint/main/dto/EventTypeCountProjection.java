package com.netpoint.main.dto;

import com.netpoint.main.models.AuditLog;

public interface EventTypeCountProjection {
    AuditLog.EventType getEventType();
    long getCount();
}
