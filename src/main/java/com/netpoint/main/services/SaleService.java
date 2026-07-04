package com.netpoint.main.services;

import com.netpoint.main.dto.SaleDTO;
import com.netpoint.main.models.Sale;
import com.netpoint.main.repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public Page<SaleDTO> getCompanySales(Integer companyId, Pageable pageable) {
        return saleRepository.findByCompany_IdOrderByCreatedAtDesc(companyId, pageable)
                .map(this::toDTO);
    }

    private SaleDTO toDTO(Sale sale) {
        return new SaleDTO(
                sale.getId(),
                sale.getCashierNameSnapshot() != null ? sale.getCashierNameSnapshot() : "Unknown",
                sale.getCreatedAt(),
                sale.getTotalRevenue(),
                sale.getTotalCost(),
                sale.getTotalProfit(),
                sale.getItems().size()
        );
    }
}