package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.dto.responses.InvitationControllerResponse;
import com.netpoint.main.services.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<InvitationControllerResponse> invite(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody @Valid InviteRequest request) {
        invitationService.inviteUser(
                Integer.parseInt(user.userId()), request.email(), request.role(), user.companyId());
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
    public ResponseEntity<GenericResponse>
    complete(@RequestParam String token, @RequestBody @Valid CompleteRegistrationRequest request) {
        return ResponseEntity.ok(
                invitationService.completeRegistration(
                        token,
                        request.password(),
                        request.name()
                )
        );
    }

    @PatchMapping("/approve/{userToApproveId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<GenericResponse> approve(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer userToApproveId) {
        return ResponseEntity.ok(invitationService.acceptUser(userToApproveId, user.companyId()));
    }
}