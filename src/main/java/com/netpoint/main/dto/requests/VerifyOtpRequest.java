package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.NotBlank;

public class VerifyOtpRequest {

    // როცა საიტზე შედიხარ admin/owner საიტით ვერიფიკაციის დროებითი ტოკენია
    @NotBlank
    private String tempToken;

    // ეს ის კოდია რაც ტელეზე მოდის
    @NotBlank
    private String otpCode;

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}