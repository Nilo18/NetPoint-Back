package com.netpoint.main.repositories;

import com.netpoint.main.models.Sale;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SaleRepositoryCustom {
    List<Integer> findIdsBySpecification(Specification<Sale> specification);
}
