package com.netpoint.main.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private String date; // "Apr 1, 2026"
    private Double amount;
    private String status; // "Paid", "Pending", "Failed"
}