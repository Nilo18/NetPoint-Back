package com.netpoint.main.dto.requests;

import com.netpoint.main.dto.AuthenticatedUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@RequiredArgsConstructor
public class AuditLogQuery {
    private int page = 0;
    private int size = 0;
    private String eventType = "";
    private String role = "";
    private String search = "";
}
