package com.netpoint.main.repositories;

import com.netpoint.main.models.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
    List<Sale> findAllByCompany_Id(Integer companyId);
    @Query("""
        select coalesce(sum(si.lineRevenue), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.product.id in :productIds
    """)
    BigDecimal sumRevenueByCompanyIdAndProductIds(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds
    );

    @Query("""
        select coalesce(sum(si.lineRevenue), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.product.id in :productIds
          and si.sale.createdAt >= :fromDate
    """)
    BigDecimal sumTotalRevenueSince(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds,
            @Param("fromDate") LocalDateTime fromDate
    );

    @Query("""
        select coalesce(sum(si.lineRevenue), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.product.id in :productIds
          and si.sale.createdAt >= :fromDate
          and si.sale.createdAt < :toDate
    """)
    BigDecimal sumTotalRevenueBetween(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select coalesce(sum(si.lineProfit), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.product.id in :productIds
    """)
    BigDecimal sumTotalProfitByCompanyIdAndProductIds(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds
    );

    @Query("""
        select coalesce(sum(si.lineProfit), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.product.id in :productIds
          and si.sale.createdAt >= :fromDate
          and si.sale.createdAt < :toDate
    """)
    BigDecimal sumTotalProfitBetween(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
