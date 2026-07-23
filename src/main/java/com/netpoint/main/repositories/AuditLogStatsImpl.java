package com.netpoint.main.repositories;

import com.netpoint.main.dto.EventTypeCountDTO;
import com.netpoint.main.models.AuditLog;
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

@Repository
@Data
@RequiredArgsConstructor
public class AuditLogStatsImpl implements AuditLogStatsRepository{
    private final EntityManager entityManager;

    @Override
    public List<EventTypeCountDTO> countByEventType(Specification<AuditLog> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventTypeCountDTO> query = cb.createQuery(EventTypeCountDTO.class);

        Root<AuditLog> root = query.from(AuditLog.class);

        Predicate predicate = specification.toPredicate(root, query, cb);

        query.select(cb.construct(
                EventTypeCountDTO.class,
                root.get("eventType"),
                cb.count(root)
        ));

        if (predicate != null) {
            query.where(predicate);
        }

        query.groupBy(root.get("eventType"));

        return entityManager.createQuery(query).getResultList();
    }
}
