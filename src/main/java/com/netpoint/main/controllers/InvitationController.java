package com.netpoint.main.controllers;

import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.InvitationControllerResponse;
import com.netpoint.main.services.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.netpoint.main.dto.requests.InviteRequest;
import com.netpoint.main.dto.requests.CompleteRegistrationRequest;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    // mowveva igzavneba
    @PostMapping("/invite")
    // xo owneria magas naxulobs
    public ResponseEntity<InvitationControllerResponse> invite(@RequestBody @Valid InviteRequest request) {
        invitationService.inviteUser(request.email(), request.role(), request.companyId());
        return ResponseEntity.ok(new InvitationControllerResponse(200,"Invitation sent"));
    }

    // tokenis validurobis shesamowmebelia
    @GetMapping("/validate")
    public ResponseEntity<InvitationControllerResponse> validate(@RequestParam String token) {
        String companyName = this.invitationService.validateInvitation(token);
        return ResponseEntity.ok(new InvitationControllerResponse(
                200, "Welcome to " + companyName + "!"
        ));
    }

    // admins info sheyavs
    @PostMapping("/complete")
    public ResponseEntity<AuthResponse>
    complete(@RequestParam String token, @RequestBody @Valid CompleteRegistrationRequest request) {
        return ResponseEntity.ok(
                invitationService.completeRegistration(
                        token,
                        request.password(),
                        request.name()
                )
        );
    }
}