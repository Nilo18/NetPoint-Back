package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.requests.AddPaymentMethodRequest;
import com.netpoint.main.dto.requests.UpdatePaymentMethodRequest;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.dto.responses.PaymentMethodResponse;
import com.netpoint.main.services.PaymentMethodService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Data
@RestController
@RequestMapping("/api/payment-method")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethod(
            @AuthenticationPrincipal AuthenticatedUser user) {
        PaymentMethodResponse pm =
                paymentMethodService.getPaymentMethod(user.companyId().intValue());
        return ResponseEntity.ok(pm);   // tu carielia, anu karta araaqvs
    }

    @PostMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody AddPaymentMethodRequest request) {
        return ResponseEntity.ok(
                paymentMethodService.addPaymentMethod(user.companyId().intValue(), request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<PaymentMethodResponse> updatePaymentMethod(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody UpdatePaymentMethodRequest request) {
        return ResponseEntity.ok(
                paymentMethodService.updatePaymentMethod(user.companyId().intValue(), request));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<GenericResponse> deletePaymentMethod(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(paymentMethodService.deletePaymentMethod(user.companyId().intValue()));
    }
}