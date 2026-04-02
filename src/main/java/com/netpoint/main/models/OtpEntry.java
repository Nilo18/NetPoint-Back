package com.netpoint.main.models;

import java.time.Instant;

public class OtpEntry {
    private final String userId;
    private final String phoneNumber;
    private final String otpCode;
    private final Instant expiresAt;
    private int attempts;

    public OtpEntry(String userId, String phoneNumber, String otpCode) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.expiresAt = Instant.now().plusSeconds(300); // 5 minutes
        this.attempts = 0;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    public String getUserId() { return userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getOtpCode() { return otpCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getAttempts() { return attempts; }
    public void incrementAttempts() { this.attempts++; }
}