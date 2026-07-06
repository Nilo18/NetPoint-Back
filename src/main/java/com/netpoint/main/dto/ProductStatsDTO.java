package com.netpoint.main.dto;

import java.math.BigDecimal;

public record ProductStatsDTO(
        BigDecimal totalRevenue,
        BigDecimal increaseFromLastMonth,
        BigDecimal netProfit,
        BigDecimal margin,
        String topSellingItem,
        Integer unitsSold,
        Integer lowStockItemCount
    ) {
}
