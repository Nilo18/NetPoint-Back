package com.netpoint.main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyDTO(
        Integer id,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotBlank
        String industry) {
}
