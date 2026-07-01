package com.netpoint.main.repositories;

import com.netpoint.main.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByCompany_IdOrderByOccurredAtDesc(Integer companyId, Pageable pageable);
}
