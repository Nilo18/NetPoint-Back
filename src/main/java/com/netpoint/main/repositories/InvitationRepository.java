package com.netpoint.main.repositories;

import com.netpoint.main.models.Company;
import com.netpoint.main.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmailAndCompanyId(String email, Long companyId);
    boolean existsByToken(String token);
    boolean existsByEmailAndCompanyIdAndUsedFalse(String email, Integer companyId);
    boolean existsByEmailAndCompanyIdAndUsedFalseAndExpiresAtAfter(String email, Integer companyId, LocalDateTime now);
}