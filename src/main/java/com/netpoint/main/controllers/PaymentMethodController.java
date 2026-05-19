package com.netpoint.main.controllers;


import com.netpoint.main.dto.requests.UpdatePaymentMethodRequest;
import com.netpoint.main.dto.responses.PaymentMethodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-method")
public class PaymentMethodController {

    // აბრუნებს ახლანდელ მეთოდს
    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethod() {
        PaymentMethodResponse paymentMethod = new PaymentMethodResponse(
                "VISA",
                "4242",
                "12/26"
        );
        return ResponseEntity.ok(paymentMethod);
    }

    // PUT - გადახდის მეთოდს ააფდეითებს
    @PutMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<String> updatePaymentMethod(@RequestBody UpdatePaymentMethodRequest request) {

        return ResponseEntity.ok("Payment method updated successfully");
    }
}