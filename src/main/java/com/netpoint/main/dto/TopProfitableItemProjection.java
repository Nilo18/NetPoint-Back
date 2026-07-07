package com.netpoint.main.dto;

import java.math.BigDecimal;

public interface TopProfitableItemProjection {
    String getProductName();
    BigDecimal getTotalProfit();
}
