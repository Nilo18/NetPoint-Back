package com.netpoint.main.dto;

import java.math.BigDecimal;

public record SaleItemDTO(
    Integer productId,
    String productName,
    Integer quantity,
    BigDecimal unitRetailPrice,
    BigDecimal lineRevenue
) {
}
