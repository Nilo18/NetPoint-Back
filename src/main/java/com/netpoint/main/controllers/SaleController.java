package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.SaleDTO;
import com.netpoint.main.dto.requests.SalesQuery;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<SaleDTO>> getCompanySales(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ModelAttribute SalesQuery query) {

        return ResponseEntity.ok(
                PageResponse.from(saleService.getCompanySales(user.companyId(), query))
        );
    }
}