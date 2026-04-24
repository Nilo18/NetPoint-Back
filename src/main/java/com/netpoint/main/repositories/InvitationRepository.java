package com.netpoint.main.repositories;

import com.netpoint.main.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmailAndCompanyId(String email, Long companyId);
    boolean existsByToken(String token);
}