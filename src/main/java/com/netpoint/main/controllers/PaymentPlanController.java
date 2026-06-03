package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.PaymentPlanDTO;
import com.netpoint.main.dto.requests.ChangePlanRequest;
import com.netpoint.main.services.PaymentPlanService;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Data
@RestController
@Log
@RequestMapping(path = "/api/payment-plan")
public class PaymentPlanController {
    private final PaymentPlanService paymentPlanService;

    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<PaymentPlanDTO> getPaymentPlan(@AuthenticationPrincipal AuthenticatedUser user) {
        Integer companyId = user.companyId().intValue();
        log.info("companyId IS: " + companyId);
        return ResponseEntity.ok(paymentPlanService.getPaymentPlan(companyId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<String> changePlan(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ChangePlanRequest request) {
        Integer companyId = user.companyId().intValue();
        paymentPlanService.changePlan(companyId, request.getNewPlanName());
        return ResponseEntity.ok("Plan changed to: " + request.getNewPlanName());
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<String> cancelSubscription(@AuthenticationPrincipal AuthenticatedUser user) {
        Integer companyId = user.companyId().intValue();
        paymentPlanService.cancelSubscription(companyId);
        return ResponseEntity.ok("Subscription cancelled successfully");
    }
}
