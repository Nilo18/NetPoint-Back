package com.netpoint.main.dto.responses;

public record AuditLogStatsResponse(
        Long totalEvents,
        Long sales,
        Long productChanges,
        Long teamChanges,
        Long paymentChanges,
        Long accountChanges,
        Long companyChanges
) {
}
