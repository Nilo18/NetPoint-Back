package com.netpoint.main.dto.requests;

import com.netpoint.main.dto.CompanyDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CompanyUpdateRequest(
        @NotNull
        Integer id,
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String industry,
        @Valid VerifyOtpRequest verificationInfo
) {}