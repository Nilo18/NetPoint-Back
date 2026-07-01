package com.netpoint.main.services;

import com.netpoint.main.dto.AuditLogDTO;
import com.netpoint.main.models.AuditLog;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Company company, User actor, AuditLog.EventType eventType, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setCompany(company);
        auditLog.setUser(actor);
        auditLog.setEventType(eventType);
        auditLog.setDetails(details);
        auditLog.setActorNameSnapshot(actor != null ? actor.getName() : null);
        auditLog.setActorRoleSnapshot(actor != null ? actor.getRole() : null);
        auditLog.setCompanyNameSnapshot(company != null ? company.getName() : null);
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogDTO> getCompanyAuditLogs(Integer companyId, Pageable pageable) {
        return auditLogRepository.findByCompany_IdOrderByOccurredAtDesc(companyId, pageable)
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getEventType(),
                        log.getDetails(),
                        log.getActorNameSnapshot(),
                        log.getActorRoleSnapshot(),
                        log.getOccurredAt()
                ));
    }
}
