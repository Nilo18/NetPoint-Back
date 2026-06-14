package com.netpoint.main.dto.responses;

import com.netpoint.main.dto.SaleItemDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        String userName,
        BigDecimal totalRevenue,
        LocalDateTime createdAt,
        List<SaleItemDTO> item
) {
}
