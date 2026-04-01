package com.netpoint.main.services;

import com.netpoint.main.models.OtpEntry;

public interface OtpStore {
    String save(String userId, String phoneNumber, String otpCode);
    OtpEntry get(String tempToken);
    void invalidate(String tempToken);
}