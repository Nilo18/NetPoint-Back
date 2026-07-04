package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.SaleDTO;
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
@RequestMapping("/settings/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/{companyId}")
    public ResponseEntity<PageResponse<SaleDTO>> getCompanySales(
            @PathVariable Integer companyId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!user.companyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                PageResponse.from(saleService.getCompanySales(companyId, PageRequest.of(page, size)))
        );
    }
}