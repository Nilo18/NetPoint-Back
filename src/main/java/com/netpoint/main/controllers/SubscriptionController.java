package com.netpoint.main.controllers;



import com.netpoint.main.dto.requests.ChangePlanRequest;
import com.netpoint.main.dto.responses.SubscriptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    // GET ახლანდელი საბსქრიბშენი
    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {
        SubscriptionResponse subscription = new SubscriptionResponse(
                "Professional Plan",
                "For growing businesses",
                49.0,
                "month",
                Arrays.asList(
                        "Unlimited products",
                        "Up to 10 team members",
                        "Advanced analytics",
                        "Priority support"
                )
        );
        return ResponseEntity.ok(subscription);
    }


    @PutMapping("/plan")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<String> changePlan(@RequestBody ChangePlanRequest request) {

        return ResponseEntity.ok("Plan changed to: " + request.getNewPlanName());
    }

    // DELETE - საბსქრიბშინს შლის
    @DeleteMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<String> cancelSubscription() {

        return ResponseEntity.ok("Subscription cancelled successfully");
    }
}
