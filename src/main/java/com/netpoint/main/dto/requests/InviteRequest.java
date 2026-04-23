package com.netpoint.main.dto.requests;

public record InviteRequest(
        String email,
        String role,
        Integer companyId
    ) {
}