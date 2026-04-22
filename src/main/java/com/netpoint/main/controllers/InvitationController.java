package com.netpoint.main.controllers;

import com.netpoint.main.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.netpoint.main.models.InviteRequest;
import com.netpoint.main.models.CompleteRegistrationRequest;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    // mowveva igzavneba
    @PostMapping("/invite")
    // xo owneria magas naxulobs
    public ResponseEntity<String> invite(@RequestBody InviteRequest request) {
        invitationService.inviteUser(request.getEmail(), request.getRole(), request.getCompanyId());
        return ResponseEntity.ok("Invitation sent");
    }

    // tokenis validurobis shesamowmebelia
    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestParam String token) {

        return ResponseEntity.ok("Valid");
    }

    // admins info sheyavs
    @PostMapping("/complete")
    public ResponseEntity<String> complete(@RequestBody CompleteRegistrationRequest request) {
        invitationService.completeRegistration(
                request.getToken(),
                request.getPassword(),
                request.getFullName()
        );
        return ResponseEntity.ok("Account created");
    }
}