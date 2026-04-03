package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public record VerifyOtpRequest(
    @NotBlank String tempToken,
    @NotBlank String otpCode) {
}