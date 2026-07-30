package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupFirstStageRequest(
        @NotBlank @Email String companyEmail,
        @NotBlank @Email String userEmail
) {
}
