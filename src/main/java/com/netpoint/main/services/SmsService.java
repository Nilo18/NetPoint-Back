package com.netpoint.main.services;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//ეს გამოიყენება რო ტვილიოს გამოყენებით გავგზავნოთ უკვე პირად ნომერზე 2ფა
@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.phone.number}")
    private String fromPhone;

    @PostConstruct
    public void init() {
//ტვილიოს სერვერით აუთენტიკაცია შენი ექაუნთის რაღაცებით
            Twilio.init(accountSid, authToken);

    }

    //პროსტა ტექტს გზავნის ნომერზე
    public void sendOtp(String toPhoneNumber, String otpCode) {
        Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhone),
                "Your NetPoint verification code is: " + otpCode
        ).create();
    }
}

