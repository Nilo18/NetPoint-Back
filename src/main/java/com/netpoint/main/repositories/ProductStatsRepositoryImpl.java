package com.netpoint.main.repositories;

import com.netpoint.main.dto.ProductStatsDTO;
import com.netpoint.main.dto.TopSellingItemDTO;
import com.netpoint.main.dto.TopSellingItemProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductStatsRepositoryImpl implements ProductStatsRepository {
    private final SaleItemRepository saleItemRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    @Override
    public BigDecimal getTotalRevenue(Integer companyId, List<Integer> productIds) {
        return saleRepository.sumRevenueByCompanyIdAndProductIds(companyId, productIds);
    }

    @Override
    public BigDecimal getRevenueForCurrentMonth(Integer companyId, List<Integer> productIds) {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfCurrentMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();

        return saleRepository.sumTotalRevenueBetween(companyId, productIds, startOfCurrentMonth, startOfNextMonth);
    }

    @Override
    public BigDecimal getRevenueForLastMonth(Integer companyId, List<Integer> productIds) {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfCurrentMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfPreviousMonth = today.withDayOfMonth(1).
                minusMonths(1).atStartOfDay();

        return saleRepository.sumTotalRevenueBetween(companyId, productIds, startOfPreviousMonth, startOfCurrentMonth);
    }

    @Override
    public BigDecimal getNetProfit(Integer companyId, List<Integer> productIds) {
        return saleRepository.sumTotalProfitByCompanyIdAndProductIds(companyId, productIds);
    }

    @Override
    public TopSellingItemDTO getTopSellingItem(Integer companyId, List<Integer> productIds) {
        TopSellingItemProjection topSellingItem = saleItemRepository.findTopSellingItemByCompanyId(companyId, productIds);

        String name = topSellingItem == null ? null : topSellingItem.getProductName();
        Integer itemsSold = topSellingItem == null ? 0 : Math.toIntExact(topSellingItem.getUnitsSold());

        return new TopSellingItemDTO(name, itemsSold);
    }

    @Override
    public BigDecimal calculateRevenueIncreasePercent(BigDecimal currentMonthRevenue, BigDecimal lastMonthRevenue) {
        currentMonthRevenue = currentMonthRevenue == null ? BigDecimal.ZERO : currentMonthRevenue;
        lastMonthRevenue = lastMonthRevenue == null ? BigDecimal.ZERO : lastMonthRevenue;

        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return currentMonthRevenue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(100);
        }

        return currentMonthRevenue
                .subtract(lastMonthRevenue)
                .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public BigDecimal calculateNetProfitMargin(BigDecimal netProfit, BigDecimal totalRevenue) {
        netProfit = netProfit == null ? BigDecimal.ZERO : netProfit;
        totalRevenue = totalRevenue == null? BigDecimal.ZERO : totalRevenue;

        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return netProfit
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long countLowStockCount(Integer companyId, List<Integer> productIds) {
        return productRepository.countByCompany_IdAndIdInAndStockLessThan(companyId, productIds, 50);
    }

    @Override
    public ProductStatsDTO getStats(Integer companyId, List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return new ProductStatsDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    0,
                    0
            );
        }

        BigDecimal totalRevenue = getTotalRevenue(companyId, productIds);
        BigDecimal currentMonthRevenue = getRevenueForCurrentMonth(companyId, productIds);
        BigDecimal lastMonthRevenue = getRevenueForLastMonth(companyId, productIds);
        BigDecimal revenueIncrease = calculateRevenueIncreasePercent(currentMonthRevenue, lastMonthRevenue);
        BigDecimal netProfit = getNetProfit(companyId, productIds);
        BigDecimal netProfitMargin = calculateNetProfitMargin(netProfit, totalRevenue);
        TopSellingItemDTO topSellingItem = getTopSellingItem(companyId, productIds);
        Long lowStockCount = countLowStockCount(companyId, productIds);

        return new ProductStatsDTO(
                totalRevenue,
                revenueIncrease,
                netProfit,
                netProfitMargin,
                topSellingItem.name(),
                topSellingItem.unitsSold(),
                Math.toIntExact(lowStockCount)
        );
    }
}
