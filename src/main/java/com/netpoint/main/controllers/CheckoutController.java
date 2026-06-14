package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.requests.CheckoutRequest;
import com.netpoint.main.dto.responses.SaleResponse;
import com.netpoint.main.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping(path = "/api/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<SaleResponse> checkout(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(checkoutService.checkout(
                user.companyId().intValue(), Integer.valueOf(user.userId()), request
        ));
    }
}
