package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String role,
        @NotNull
        Integer companyId
    ) {
}