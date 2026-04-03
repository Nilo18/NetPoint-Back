package com.netpoint.main.services;

import com.netpoint.main.exceptions.OtpExpiredException;
import com.netpoint.main.exceptions.OtpNotFoundException;
import com.netpoint.main.models.OtpEntry;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryOtpStore implements OtpStore {

    // ეს იმისთვისაა როცა ბევრი ერთდროულად ცდილობს საიტზე შესვლას
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public String save(String userId, String phoneNumber, String otpCode) {
        //ეს აგენერირებს იმ ტემპტოკენს რასაც ვიყენებთ მერე 2ფა-სთვის
        String tempToken = UUID.randomUUID().toString();
        //ინიახავს იუზერ ინფოს და ტემპ ტოკენს აბრუნებს
        store.put(tempToken, new OtpEntry(userId, phoneNumber, otpCode));
        return tempToken;
    }

    @Override
    public OtpEntry get(String tempToken) {
        OtpEntry entry = store.get(tempToken);
        //თუ ტოკენი არაა მაშინ ამთავრებს
        if (entry == null) {
            throw new OtpNotFoundException("Invalid or expired token");
        }
        //ამოწმებს რომ ჯერ ვადა არ აქვს გასული
        if (entry.isExpired()) {
            store.remove(tempToken);
            throw new OtpExpiredException("OTP has expired");
        }

        return entry;
    }

    @Override
    public void invalidate(String tempToken) {
        //საიტზე შესვლის მერე შლის ამ ტოკენს
        store.remove(tempToken);
    }
}