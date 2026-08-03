package com.netpoint.main.services;

import com.netpoint.main.dto.SaleDTO;
import com.netpoint.main.dto.SaleItemDTO;
import com.netpoint.main.dto.requests.SalesQuery;
import com.netpoint.main.dto.requests.SalesStatsQuery;
import com.netpoint.main.dto.responses.SalesStatsResponse;
import com.netpoint.main.exceptions.BadRequestException;
import com.netpoint.main.models.Sale;
import com.netpoint.main.models.SaleItem;
import com.netpoint.main.repositories.SaleItemRepository;
import com.netpoint.main.repositories.SaleRepository;
import com.netpoint.main.repositories.SaleRepositoryCustom;
import com.netpoint.main.repositories.SalesStatsRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleRepositoryCustom saleRepositoryCustom;
    private final SalesStatsRepository salesStatsRepository;

    public Specification<Sale> companyIs(Integer companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) return null;
            return criteriaBuilder.equal(root.get("company").get("id"), companyId);
        };
    }

    public Specification<Sale> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String normalized = search.trim();
            String pattern = "%" + normalized.toLowerCase() + "%";

            Predicate namePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("cashierNameSnapshot")), pattern
            );

            Predicate idPredicate = criteriaBuilder.disjunction();

            if (normalized.matches("\\d{1,10}")) {
                long parsedId = Long.parseLong(normalized);

                if (parsedId <= Integer.MAX_VALUE) {
                    idPredicate = criteriaBuilder.equal(
                            root.get("id"),
                            (int) parsedId
                    );
                }
            }

            Join<Sale, SaleItem> itemJoin = root.join("items", JoinType.LEFT);

            Predicate itemPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(itemJoin.get("productNameSnapshot")), pattern
            );

            query.distinct(true);

            return criteriaBuilder.or(idPredicate, namePredicate, itemPredicate);
        };
    }

    private BigDecimal parseRangeValue(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Filter values must be valid numbers");
        }
    }

    private Specification<Sale> decimalRange(
        String fieldName, String filterFrom, String filterTo) {

        BigDecimal from = parseRangeValue(filterFrom);
        BigDecimal to = parseRangeValue(filterTo);

        if (from.compareTo(to) > 0) {
            throw new BadRequestException("Filter from can't exceed filter to");
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(fieldName), from, to);
    }

    private Specification<Sale> dateRange(String filterFrom, String filterTo) {
        LocalDate createdAtDate = LocalDate.parse(filterFrom);
        LocalDate createFromDate = LocalDate.parse(filterTo);

        if (createdAtDate.isAfter(createFromDate)) {
            throw new BadRequestException("Created from date cannot exceed created to date");
        }

        LocalDateTime start = createdAtDate.atStartOfDay();
        LocalDateTime end = createFromDate.atTime(LocalTime.MAX);

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("createdAt"), start, end);
    }

    public Specification<Sale> filter(String filterBy, String filterFrom, String filterTo) {
        return switch (filterBy.toLowerCase()) {
            case "", "totalrevenue", "totalcost", "totalprofit", "marginpercent" ->
                    decimalRange(filterBy, filterFrom, filterTo);
            case "date" -> dateRange(filterFrom, filterTo);
            default -> throw new BadRequestException("Unsupported filter field: " + filterBy);
        };
    }

    public Pageable createPageable(SalesQuery query) {
        String property = switch (query.getSortBy().toLowerCase()) {
            case "", "date" -> "createdAt";
            case "revenue" -> "totalRevenue";
            case "profit" -> "totalProfit";
            case "cost" -> "totalCost";
            case "margin" -> "marginPercent";
            default -> throw new BadRequestException("Unsupported sort field" + query.getSortBy());
        };

        Sort.Direction direction = switch (query.getSortDirection().toLowerCase()) {
            case "asc" -> Sort.Direction.ASC;
            case "", "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException("Unsupported sort direction: " + query.getSortDirection());
        };

        return PageRequest.of(query.getPage(), query.getSize(), Sort.by(direction, property));
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> getCompanySales(Integer companyId, SalesQuery query) {
        if (query.getSize() > 100) {
            throw new BadRequestException("Requested page size is too large.");
        }

        Pageable pageable = createPageable(query);

        Specification<Sale> specification = Specification.where(companyIs(companyId))
                .and(matchesSearch(query.getSearch()));

        if (query.getFilterFrom() != null && !query.getFilterBy().isBlank()) {
            specification = specification.and(
                filter(query.getFilterBy(), query.getFilterFrom(), query.getFilterTo())
            );
        }

        Page<Sale> sales = saleRepository.findAll(specification, pageable);
        List<Integer> saleIds = sales.getContent().stream().map(Sale::getId).toList();

        List<SaleItem> saleItems = saleItemRepository.findBySale_IdIn(saleIds);

        Map<Integer, List<SaleItemDTO>> itemsBySaleId = saleItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getSale().getId(),
                        Collectors.mapping(this::toSaleItemDTO, Collectors.toList())
                ));

        return saleRepository.findAll(specification, pageable)
                .map(sale -> new SaleDTO(
                        sale.getId(),
                        sale.getCashierNameSnapshot(),
                        sale.getCreatedAt(),
                        sale.getTotalRevenue(),
                        sale.getTotalCost(),
                        sale.getTotalProfit(),
                        sale.getMarginPercent(),
                        itemsBySaleId.getOrDefault(sale.getId(), List.of())
                ));
    }

    public SalesStatsResponse getCompanySalesStats(Integer companyId, SalesStatsQuery query) {
        Specification<Sale> specification = Specification.where(companyIs(companyId))
                .and(matchesSearch(query.getSearch()));

        if (query.getFilterFrom() != null && !query.getFilterBy().isBlank()) {
            specification = specification.and(
                    filter(query.getFilterBy(), query.getFilterFrom(), query.getFilterTo())
            );
        }

        List<Integer> saleIds = saleRepositoryCustom.findIdsBySpecification(specification);

        return salesStatsRepository.getSalesStats(companyId, saleIds);
    }

    private SaleDTO toDTO(Sale sale) {
        Integer saleId = sale.getId();

        List<SaleItem> saleItems = saleItemRepository.findBySale_Id(saleId);
        List<SaleItemDTO> saleItemDTOS = saleItems.stream().map(saleItem ->
                new SaleItemDTO(
                        saleItem.getId(),
                        saleItem.getProductNameSnapshot(),
                        saleItem.getQuantity(),
                        saleItem.getUnitRetailPrice(),
                        saleItem.getLineRevenue()
                )
        ).toList();

        return new SaleDTO(
                sale.getId(),
                sale.getCashierNameSnapshot() != null ? sale.getCashierNameSnapshot() : "Unknown",
                sale.getCreatedAt(),
                sale.getTotalRevenue(),
                sale.getTotalCost(),
                sale.getTotalProfit(),
                sale.getMarginPercent(),
                saleItemDTOS
        );
    }

    private SaleItemDTO toSaleItemDTO(SaleItem saleItem) {
        return new SaleItemDTO(
                saleItem.getId(),
                saleItem.getProductNameSnapshot(),
                saleItem.getQuantity(),
                saleItem.getUnitRetailPrice(),
                saleItem.getLineRevenue()
        );
    }
}