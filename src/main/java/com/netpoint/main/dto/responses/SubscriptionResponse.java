package com.netpoint.main.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionResponse {
    private String planName;
    private String description;
    private Double price;
    private String billingPeriod; // "month", "year"
    private List<String> features;
}