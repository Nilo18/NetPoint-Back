package com.netpoint.main.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleDTO(
        Integer id,
        String cashierName,
        LocalDateTime createdAt,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal totalProfit,
        BigDecimal marginPercent,
        List<SaleItemDTO> saleItems
) {}