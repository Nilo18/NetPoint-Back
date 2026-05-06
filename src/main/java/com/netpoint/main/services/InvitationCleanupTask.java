package com.netpoint.main.services;

import com.netpoint.main.repositories.InvitationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// ეს კლასი ვადაგასულ მოწვევებს პერიოდულად შლის
@Component
@Log
@AllArgsConstructor
public class InvitationCleanupTask {

    private final InvitationRepository invitationRepository;

    //ყოველ 60 წამში ერთხელ გაეშვება

    @Scheduled(fixedRate = 60000)
    public void deleteExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = invitationRepository.deleteAllExpiredBefore(now);
        if (deleted > 0) {
            log.info("Deleted " + deleted + " expired invitations at " + now);
        }
    }
}