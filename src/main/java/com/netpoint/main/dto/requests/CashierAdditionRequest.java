package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CashierAdditionRequest(
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String role,
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "PIN must be exactly 6 digits")
        String pin,
        @NotNull
        Integer companyId
    ) {
}
