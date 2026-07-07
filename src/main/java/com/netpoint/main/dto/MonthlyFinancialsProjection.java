package com.netpoint.main.dto;

import java.math.BigDecimal;

public interface MonthlyFinancialsProjection {
    Integer getMonthNumber();
    BigDecimal getRevenue();
    BigDecimal getProfit();
}
