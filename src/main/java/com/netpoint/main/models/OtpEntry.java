package com.netpoint.main.models;

import lombok.Data;

import java.time.Instant;

@Data
public class OtpEntry {
    private final String userId;
//    private final String phoneNumber;
    private final String otpCode;
    private final Instant expiresAt;
    private int attempts;

    public OtpEntry(String userId, String otpCode) {
        this.userId = userId;
//        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.expiresAt = Instant.now().plusSeconds(300); // 5 minutes
        this.attempts = 0;
    }

    public OtpEntry(String otpCode) {
        this(null, otpCode);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void incrementAttempts() { this.attempts++; }
}