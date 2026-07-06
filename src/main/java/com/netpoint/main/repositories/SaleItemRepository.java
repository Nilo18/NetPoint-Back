package com.netpoint.main.repositories;

import com.netpoint.main.dto.TopSellingItemProjection;
import com.netpoint.main.models.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    List<SaleItem> findAllBySale_Company_Id(Integer companyId);

    @Query(value = """
        select
            si.product_name_snapshot as productName,
            sum(si.quantity) as unitsSold
        from sale_items si
        join sales s on s.id = si.sale_id
        where s.company_id = :companyId
          and si.product_id in (:productIds)
        group by si.product_id, si.product_name_snapshot
        order by sum(si.quantity) desc
        limit 1
    """, nativeQuery = true)
    TopSellingItemProjection findTopSellingItemByCompanyId(
            @Param("companyId") Integer companyId,
            @Param("productIds") List<Integer> productIds
    );
}
