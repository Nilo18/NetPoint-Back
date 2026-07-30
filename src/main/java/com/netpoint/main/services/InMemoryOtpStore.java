package com.netpoint.main.services;

import com.netpoint.main.exceptions.InvalidOtpException;
import com.netpoint.main.exceptions.OtpExpiredException;
import com.netpoint.main.exceptions.OtpNotFoundException;
import com.netpoint.main.models.OtpEntry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
@RequiredArgsConstructor
public class InMemoryOtpStore implements OtpStore {
    private final EmailService emailService;
//    private final OtpStore otpStore;

    // ეს იმისთვისაა როცა ბევრი ერთდროულად ცდილობს საიტზე შესვლას
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public String save(String userId, String otpCode) {
        //ეს აგენერირებს იმ ტემპტოკენს რასაც ვიყენებთ მერე 2ფა-სთვის
        String tempToken = UUID.randomUUID().toString();
        //ინიახავს იუზერ ინფოს და ტემპ ტოკენს აბრუნებს
        store.put(tempToken, new OtpEntry(userId, otpCode));
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

    @Override
    public String generateAndSend(String email, String subject) {
        // agenerirebs 6 cifra kods
        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        String tempToken = save(email, otp);

        //gzavnis ukve imeilze
        emailService.sendOtpEmail(email, otp, subject);

        return tempToken;
    }

    @Override
    public void validate(String otpCode, String tempToken) {
        OtpEntry entry = get(tempToken);
        //თუ ოტპ არასწორია იმწამსვე უარყოფს და შლის
        if (!entry.getOtpCode().equals(otpCode)) {
            invalidate(tempToken);
            throw new InvalidOtpException("Invalid verification code.");
        }

//        log.info("Checking entry.isExpired() value: " + entry.isExpired());
        if (entry.isExpired()) {
//            log.info("Throwing verification code expired error...");
            throw new OtpExpiredException("Verification code expired.");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        invalidate(tempToken);
    }
}