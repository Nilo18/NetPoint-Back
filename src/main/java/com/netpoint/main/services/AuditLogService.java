package com.netpoint.main.services;

import com.netpoint.main.dto.AuditLogDTO;
import com.netpoint.main.dto.requests.AuditLogQuery;
import com.netpoint.main.exceptions.BadRequestException;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.models.AuditLog;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.AuditLogRepository;
import com.netpoint.main.repositories.CompanyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;

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

    private static Specification<AuditLog> companyIs(Integer companyId) {
        return (root, query, cb) -> {
            if (companyId == null) return null;
            return cb.equal(root.get("company").get("id"), companyId);
        };
    }

    private static Specification<AuditLog> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            Predicate namePredicate = cb.like(
                    cb.lower(root.get("actorNameSnapshot")), pattern
            );

            Predicate detailPredicate = cb.like(
                    cb.lower(root.get("details")), pattern
            );

            return cb.or(namePredicate, detailPredicate);
        };
    }

    private static Specification<AuditLog> filterEventType(String eventType) {
        return (root, query, cb) -> {
            if(eventType == null || eventType.trim().isEmpty()) {
                return null;
            }

            String normalizedEventType = eventType.toUpperCase().trim();

            if (!AuditLog.EventType.contains(eventType)) {
               return cb.disjunction();
            }

            AuditLog.EventType enumEventType = AuditLog.EventType.valueOf(normalizedEventType);

            return cb.equal(root.get("eventType"), enumEventType);
        };
    }

    private static Specification<AuditLog> filterRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.trim().isEmpty()) {
                return null;
            }

            String normalizedRole = role.toUpperCase().trim();

            if (!User.Role.contains(role)) {
                return cb.disjunction();
            }

            return cb.equal(root.get("actorRoleSnapshot"), normalizedRole);
        };
    }

    public Page<AuditLogDTO> getCompanyAuditLogs(Integer companyId, AuditLogQuery query) {
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Company not found");
        }

        if (!AuditLog.EventType.contains(query.getEventType()) && !query.getEventType().isEmpty()) {
            throw new BadRequestException("Unsupported event type");
        }

        if (!User.Role.contains(query.getRole()) && !query.getRole().isEmpty()) {
            throw new BadRequestException("Unsupported role");
        }

        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());

        Specification<AuditLog> specification = Specification
                .where(companyIs(companyId))
                .and(matchesSearch(query.getSearch()))
                .and(filterEventType(query.getEventType()))
                .and(filterRole(query.getRole()));

        return auditLogRepository.findAll(specification, pageable)
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
