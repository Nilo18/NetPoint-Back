package com.netpoint.main.dto.responses;

public record SignupAuthResponse(
        String status,
        String companyTempToken,
        String userTempToken
) {
}
