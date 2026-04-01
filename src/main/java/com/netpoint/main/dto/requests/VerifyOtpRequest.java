package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.NotBlank;

public class VerifyOtpRequest {
    @NotBlank
    private String tempToken;
    @NotBlank
    private String otpCode;

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}