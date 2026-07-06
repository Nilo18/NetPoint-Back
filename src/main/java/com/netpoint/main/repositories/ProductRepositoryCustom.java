package com.netpoint.main.repositories;

import com.netpoint.main.models.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Integer> findIdsBySpecification(Specification<Product> specification);
}
