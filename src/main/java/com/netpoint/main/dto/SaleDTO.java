package com.netpoint.main.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleDTO(
        Integer id,
        String cashierName,
        LocalDateTime createdAt,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal totalProfit,
        int itemCount
) {}