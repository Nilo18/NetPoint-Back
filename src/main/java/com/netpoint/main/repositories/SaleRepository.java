package com.netpoint.main.repositories;

import com.netpoint.main.dto.MonthlyFinancialsProjection;
import com.netpoint.main.models.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer>, JpaSpecificationExecutor<Sale> {
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
        select coalesce(sum(si.lineRevenue), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.sale.createdAt >= :fromDate
          and si.sale.createdAt < :toDate
    """)
    BigDecimal sumTotalRevenueBetween(
            @Param("companyId") Integer companyId,
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

    @Query("""
        select coalesce(sum(si.lineProfit), 0)
        from SaleItem si
        where si.sale.company.id = :companyId
          and si.sale.createdAt >= :fromDate
          and si.sale.createdAt < :toDate
    """)
    BigDecimal sumTotalProfitBetween(
            @Param("companyId") Integer companyId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """
        select
            cast(extract(month from s.created_at) as int) as monthNumber,
            coalesce(sum(si.line_revenue), 0) as revenue,
            coalesce(sum(si.line_profit), 0) as profit
        from sales s
        join sale_items si on si.sale_id = s.id
        where s.company_id = :companyId
          and s.created_at >= :fromDate
          and s.created_at < :toDate
        group by cast(extract(month from s.created_at) as int)
        order by monthNumber
    """, nativeQuery = true)
    List<MonthlyFinancialsProjection> findMonthlyFinancials(
            @Param("companyId") Integer companyId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Modifying
    @Query("UPDATE Sale s SET s.user = null WHERE s.user.id = :userId")
    void detachUser(@Param("userId") Integer userId);

    Page<Sale> findByCompany_IdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);
}
