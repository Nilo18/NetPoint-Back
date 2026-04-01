package com.netpoint.main.services;

import com.netpoint.main.exceptions.OtpExpiredException;
import com.netpoint.main.exceptions.OtpNotFoundException;
import com.netpoint.main.models.OtpEntry;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryOtpStore implements OtpStore {

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public String save(String userId, String phoneNumber, String otpCode) {
        String tempToken = UUID.randomUUID().toString();
        store.put(tempToken, new OtpEntry(userId, phoneNumber, otpCode));
        return tempToken;
    }

    @Override
    public OtpEntry get(String tempToken) {
        OtpEntry entry = store.get(tempToken);

        if (entry == null) {
            throw new OtpNotFoundException("Invalid or expired token");
        }
        if (entry.isExpired()) {
            store.remove(tempToken);              // clean up expired entries
            throw new OtpExpiredException("OTP has expired");
        }

        return entry;
    }

    @Override
    public void invalidate(String tempToken) {
        store.remove(tempToken);
    }
}