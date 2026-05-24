package com.netpoint.main.repositories;



import com.netpoint.main.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCompanyId(Integer companyId);
    Optional<Product> findByIdAndCompanyId(Integer id, Integer companyId);
    void deleteByIdAndCompanyId(Integer id, Integer companyId);
    void deleteByCompanyId(Integer companyId);
}
