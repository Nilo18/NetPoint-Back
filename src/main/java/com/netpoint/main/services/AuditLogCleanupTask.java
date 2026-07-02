package com.netpoint.main.services;

import com.netpoint.main.models.Company;
import com.netpoint.main.repositories.AuditLogRepository;
import com.netpoint.main.repositories.CompanyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Log
@AllArgsConstructor
public class AuditLogCleanupTask {

    private final CompanyRepository companyRepository;
    private final AuditLogRepository auditLogRepository;
    @Transactional
    @Scheduled(fixedRate = 86400000)
    public void deleteExpiredAuditLogs() {
        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            Integer retentionDays = company.getPlan() != null
                    ? company.getPlan().getAuditLogRetentionDays()
                    : null;

            if (retentionDays == null) {
                continue;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
            auditLogRepository.deleteByCompany_IdAndOccurredAtBefore(company.getId(), cutoff);
        }

        log.info("Audit log retention cleanup completed at " + LocalDateTime.now());
    }
}
