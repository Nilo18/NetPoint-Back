package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.services.CompanyService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping(path = "/api/company")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyDTO> getCompanyInfo(@AuthenticationPrincipal AuthenticatedUser user) {
        Integer companyId = user.companyId().intValue();
        return ResponseEntity.ok(companyService.getCompanyInfo(companyId));
    }
}
