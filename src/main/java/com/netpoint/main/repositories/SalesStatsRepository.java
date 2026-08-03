package com.netpoint.main.repositories;

import com.netpoint.main.dto.responses.SalesStatsResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SalesStatsRepository {
    BigDecimal getTotalMarginPercent(Integer companyId, List<Integer> salesIds,
                                     BigDecimal totalRevenue,
                                     BigDecimal totalProfit);
    BigDecimal getTotalRevenue(Integer companyId, List<Integer> salesIds);
    BigDecimal getTotalProfit(Integer companyId, List<Integer> salesIds);
    BigDecimal getTotalCost(Integer companyId, List<Integer> salesIds);
    SalesStatsResponse getSalesStats(Integer companyId, List<Integer> salesIds);
}
