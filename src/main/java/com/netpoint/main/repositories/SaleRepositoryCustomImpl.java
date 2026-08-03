package com.netpoint.main.repositories;

import com.netpoint.main.models.Sale;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Data
@RequiredArgsConstructor
@Repository
public class SaleRepositoryCustomImpl implements SaleRepositoryCustom{
    private final EntityManager entityManager;

    @Override
    public List<Integer> findIdsBySpecification(Specification<Sale> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<Sale> root = query.from(Sale.class);

        query.select(root.get("id"));

        Predicate predicate = specification.toPredicate(root, query, cb);

        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager.createQuery(query).getResultList();
    }
}
