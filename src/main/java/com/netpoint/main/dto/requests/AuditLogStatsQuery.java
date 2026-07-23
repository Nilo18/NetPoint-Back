package com.netpoint.main.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuditLogStatsQuery {
    private String eventType = "";
    private String role = "";
    private String search = "";
}
