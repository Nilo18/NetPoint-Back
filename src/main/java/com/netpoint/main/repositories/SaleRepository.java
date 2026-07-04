package com.netpoint.main.repositories;

import com.netpoint.main.models.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Modifying
    @Query("UPDATE Sale s SET s.user = null WHERE s.user.id = :userId")
    void detachUser(@Param("userId") Integer userId);
    Page<Sale> findByCompany_IdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);
}