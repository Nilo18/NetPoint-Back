package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
import com.netpoint.main.dto.requests.CompanyUpdateRequest;
import com.netpoint.main.dto.requests.UpdateAccountRequest;
import com.netpoint.main.dto.responses.InfoChangeVerificationResponse;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.services.PlanEnforcementService;
import com.netpoint.main.services.SettingsService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


@RestController
@RequestMapping(path = "/settings")
@Data
public class SettingsController {
    private final SettingsService settingsService;
    private final PlanEnforcementService planEnforcementService;


    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping(path = "/company-users/{id}")
    public ResponseEntity<PageResponse<UserDTO>> getCompanyUsers(@PathVariable Integer id,
    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                PageResponse.from(settingsService.fetchCompanyUsers(id, PageRequest.of(page, size)))
        );
    }

    @PostMapping(path = "/add-cashier")
    public ResponseEntity<PageResponse<UserDTO>> addCashier(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CashierAdditionRequest cashier,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        planEnforcementService.enforceTeamMemberLimit(cashier.companyId(), cashier.role());
        return ResponseEntity.ok(PageResponse.from(
                this.settingsService.addCashier(Integer.parseInt(user.userId()), cashier, pageable)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<UserModificationResponse> deleteUser(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer userId) {
        return ResponseEntity.ok(this.settingsService.deleteUser(Integer.parseInt(user.userId()), userId));
    }

    @PreAuthorize("hasAnyAuthority('OWNER')")
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUser(@RequestParam String searchTerm,
                                @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(this.settingsService.searchUser(searchTerm, principal.companyId()));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping(path = "/company/{companyId}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Integer companyId) {
        return ResponseEntity.ok(this.settingsService.getCompanyById(companyId));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping(path = "/company/verify")
    public ResponseEntity<InfoChangeVerificationResponse>
    verifyCompanyBusinessInfoUpdateRequest(@RequestBody @Valid CompanyDTO suggested) {
        return ResponseEntity.ok(this.settingsService.verifyCompanyUpdateRequest(suggested));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PutMapping(path = "/company")
    public ResponseEntity<CompanyDTO> updateCompanyBusinessInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody @Valid CompanyUpdateRequest suggested) {
        return ResponseEntity.ok(
                this.settingsService.updateCompanyBusinessInfo(Integer.parseInt(user.userId()), suggested));
    }

    @GetMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getAccountInfo(
            @AuthenticationPrincipal AuthenticatedUser user
            /*@PathVariable Integer userId*/) {

//        if (!Integer.valueOf(user.userId()).equals(userId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        return ResponseEntity.ok(this.settingsService.getUserAccountInfo(Integer.valueOf(user.userId())));
    }

    // ========== ACCOUNT UPDATE ==========

    @PostMapping("/account/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InfoChangeVerificationResponse>
    verifyUserInfoUpdateRequest(@RequestBody @Valid UserDTO suggested) {
        return ResponseEntity.ok(this.settingsService.verifyUserInfoUpdateRequest(suggested));
    }

    @PutMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateAccount(
            @AuthenticationPrincipal AuthenticatedUser user, // Changed type to AuthenticatedUser
            @Valid @RequestBody UpdateAccountRequest request) {

        // Parse the String userId into an Integer for your service
        Integer numericUserId = Integer.parseInt(user.userId());

        UserDTO updated = settingsService.updateAccount(numericUserId, request);
        return ResponseEntity.ok(updated);
    }

// ========== DELETE COMPANY ==========

    @DeleteMapping("/company/{companyId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Integer companyId,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        if (!user.companyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        settingsService.deleteCompany(Integer.parseInt(user.userId()), companyId);

        return ResponseEntity.noContent().build();
    }
}
