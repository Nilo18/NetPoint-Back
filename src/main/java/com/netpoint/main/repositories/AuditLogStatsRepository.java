package com.netpoint.main.repositories;

import com.netpoint.main.dto.EventTypeCountDTO;
import com.netpoint.main.models.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface AuditLogStatsRepository {
    List<EventTypeCountDTO> countByEventType(Specification<AuditLog> specification);
}
