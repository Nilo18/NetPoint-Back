package com.netpoint.main.dto.responses;

import java.math.BigDecimal;

public record SalesStatsResponse(
        Integer totalSales,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal totalProfit,
        BigDecimal marginPercent
) {}