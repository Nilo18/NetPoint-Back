package com.netpoint.main.repositories;

import com.netpoint.main.dto.EventTypeCountProjection;
import com.netpoint.main.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Modifying
    @Query("UPDATE AuditLog a SET a.user = null WHERE a.user.id = :userId")
    void detachUser(@Param("userId") Integer userId);
    Page<AuditLog> findByCompany_IdOrderByOccurredAtDesc(Integer companyId, Pageable pageable);

    void deleteByCompany_IdAndOccurredAtBefore(Integer companyId, LocalDateTime cutoff);
    long countByCompany_Id(Integer companyId);
    @Query("""
    select a.eventType as eventType, count(a) as count
    from AuditLog a
    where company.id = :companyId
    group by a.eventType
    """)
    List<EventTypeCountProjection> countByEventType(@Param("companyId") Integer companyId);
}
