package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.AuditLogDTO;
import com.netpoint.main.dto.requests.AuditLogQuery;
import com.netpoint.main.dto.requests.AuditLogStatsQuery;
import com.netpoint.main.dto.responses.AuditLogStatsResponse;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogDTO>> getCompanyAuditLogs(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ModelAttribute AuditLogQuery query) {
        if (!user.companyId().equals(user.companyId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                PageResponse.from(auditLogService.getCompanyAuditLogs(
                        user.companyId(), query)
                )
        );
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<AuditLogStatsResponse> getAuditLogStats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ModelAttribute AuditLogStatsQuery query) {
        return ResponseEntity.ok(auditLogService.getCompanyAuditLogStats(user.companyId(), query));
    }
}
