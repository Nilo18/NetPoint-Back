package com.netpoint.main.dto;

import java.util.List;

public record PaymentPlanDTO(Integer id, String planName,
                             String planPurpose, Double costPerMonth, List<String> planRules) {
}
