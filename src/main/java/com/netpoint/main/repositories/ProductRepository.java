package com.netpoint.main.repositories;



import com.netpoint.main.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByCompany_Id(Integer companyId);
    long countByCompany_Id(Integer companyId);
    Optional<Product> findByIdAndCompany_Id(Integer id, Integer companyId);
    void deleteByIdAndCompany_Id(Integer id, Integer companyId);
    void deleteByCompany_Id(Integer companyId);
    Optional<List<Product>> findByIdInAndCompany_Id(List<Integer> ids, Integer companyId);
}
