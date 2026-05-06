package com.netpoint.main.repositories;

import com.netpoint.main.models.Company;
import com.netpoint.main.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmailAndCompanyId(String email, Long companyId);
    boolean existsByToken(String token);
    boolean existsByEmailAndCompanyIdAndUsedFalse(String email, Integer companyId);
    boolean existsByEmailAndCompanyIdAndUsedFalseAndExpiresAtAfter(String email, Integer companyId, LocalDateTime now);

    // ყველა ვადაგასულ invitation-ს შლის და წაშლილების რაოდენობას აბრუნებს
    @Modifying
    @Transactional
    @Query("DELETE FROM Invitation i WHERE i.expiresAt < :now")
    int deleteAllExpiredBefore(@Param("now") LocalDateTime now);

}