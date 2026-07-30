package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegistrationRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String industry,
        @NotBlank String owner_email,
        @NotBlank String owner_name,
        @NotBlank @Size(min = 8) String owner_password,
//        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
//        String phone_number,
        @NotBlank String role,
        @NotBlank String companyOtpCode,
        @NotBlank String companyTempToken,
        @NotBlank String userOtpCode,
        @NotBlank String userTempToken
     ) {
}
