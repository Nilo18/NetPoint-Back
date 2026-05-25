package com.netpoint.main.repositories;



import com.netpoint.main.models.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Integer> {
    List<ProductAttributeValue> findByProduct_Id(Integer productId);
    void deleteByProduct_Id(Integer productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductAttributeValue v WHERE v.attribute.id = :attributeId")
    void deleteByAttributeId(@Param("attributeId") Integer attributeId);
}
