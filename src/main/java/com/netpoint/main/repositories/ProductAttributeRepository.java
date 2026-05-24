package com.netpoint.main.repositories;

import com.netpoint.main.models.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    // 1. Keep your existing working methods
    List<ProductAttribute> findByCompanyId(Integer companyId);
    Optional<ProductAttribute> findByIdAndCompanyId(Integer id, Integer companyId);
    boolean existsByAttributeNameAndCompanyId(String attributeName, Integer companyId);

    // 2. ADD THIS EXACT METHOD FOR THE DELETE CHAIN
    @Transactional
    void deleteByCompanyId(Integer companyId);
}