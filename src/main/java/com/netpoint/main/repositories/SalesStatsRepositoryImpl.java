package com.netpoint.main.repositories;

import com.netpoint.main.dto.responses.SalesStatsResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
@RequiredArgsConstructor
@Repository
@Log
public class SalesStatsRepositoryImpl implements SalesStatsRepository {
    private final SaleRepository saleRepository;

    @Override
    public BigDecimal getTotalMarginPercent(
            Integer companyId,
            List<Integer> salesIds,
            BigDecimal totalRevenue,
            BigDecimal totalProfit
    ) {
        if (salesIds == null || salesIds.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (totalProfit == null
                || totalRevenue == null
                || totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return totalProfit
                .multiply(BigDecimal.valueOf(100))
                .divide(totalRevenue, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getTotalCost(Integer companyId, List<Integer> salesIds) {
        return salesIds.isEmpty()
                ? BigDecimal.ZERO
                : saleRepository.sumTotalCost(companyId, salesIds);
    }

    @Override
    public BigDecimal getTotalRevenue(Integer companyId, List<Integer> salesIds) {
        return salesIds.isEmpty()
                ? BigDecimal.ZERO
                : saleRepository.sumTotalRevenue(companyId, salesIds);
    }

    public BigDecimal getTotalProfit(Integer companyId, List<Integer> salesIds) {
        return salesIds.isEmpty()
                ? BigDecimal.ZERO
                : saleRepository.sumTotalProfit(companyId, salesIds);
    }


    @Override
    public SalesStatsResponse getSalesStats(Integer companyId, List<Integer> salesIds) {
        Integer totalSales = salesIds.size();
        BigDecimal totalRevenue = getTotalRevenue(companyId, salesIds);
        BigDecimal totalCost = getTotalCost(companyId, salesIds);
        BigDecimal totalProfit = getTotalProfit(companyId, salesIds);
        BigDecimal marginPercent = getTotalMarginPercent(companyId, salesIds, totalRevenue, totalProfit);

        return new SalesStatsResponse(
            totalSales,
            totalRevenue,
            totalCost,
            totalProfit,
            marginPercent
        );
    }
}
