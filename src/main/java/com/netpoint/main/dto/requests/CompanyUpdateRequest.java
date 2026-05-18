package com.netpoint.main.dto.requests;

import com.netpoint.main.dto.CompanyDTO;
import jakarta.validation.Valid;

public record CompanyUpdateRequest(
        @Valid CompanyDTO newInfo,
        @Valid VerifyOtpRequest verificationInfo
) {}