package com.netpoint.main.repositories;

import com.netpoint.main.dto.ProductStatsDTO;
import com.netpoint.main.dto.TopSellingItemDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ProductStatsRepository {
    BigDecimal getTotalRevenue(Integer companyId, List<Integer> productIds);
    BigDecimal getRevenueForCurrentMonth(Integer companyId, List<Integer> productIds);
    BigDecimal getRevenueForLastMonth(Integer companyId, List<Integer> productIds);
    BigDecimal getNetProfit(Integer companyId, List<Integer> productIds);
    TopSellingItemDTO getTopSellingItem(Integer companyId, List<Integer> productIds);
    BigDecimal calculateRevenueIncreasePercent(BigDecimal currentMonthRevenue, BigDecimal lastMonthRevenue);
    BigDecimal calculateNetProfitMargin(BigDecimal netProfit, BigDecimal totalRevenue);
    Long countLowStockCount(Integer companyId, List<Integer> productIds);
    ProductStatsDTO getStats(Integer companyId, List<Integer> productIds);
}
