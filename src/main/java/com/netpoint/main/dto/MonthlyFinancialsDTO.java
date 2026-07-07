package com.netpoint.main.dto;

import java.math.BigDecimal;

public record MonthlyFinancialsDTO(
        String month,
        BigDecimal revenue,
        BigDecimal profit
) {
}
