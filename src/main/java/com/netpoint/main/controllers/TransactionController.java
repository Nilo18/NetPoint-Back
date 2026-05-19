package com.netpoint.main.controllers;



import com.netpoint.main.dto.responses.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    // ყველა ტრანზაქციას აბრუნებს
    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<List<TransactionResponse>> getTransactions() {
        List<TransactionResponse> transactions = Arrays.asList(
                new TransactionResponse(1L, "Apr 1, 2026", 49.0, "Paid"),
                new TransactionResponse(2L, "Mar 1, 2026", 49.0, "Paid"),
                new TransactionResponse(3L, "Feb 1, 2026", 49.0, "Paid")
        );
        return ResponseEntity.ok(transactions);
    }
}