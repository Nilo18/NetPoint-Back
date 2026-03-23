package com.netpoint.main.services;

import com.netpoint.main.dto.requests.LoginRequest;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
public class AuthService {
    public String login(LoginRequest request) {
        log.info("Generating token for: " + request + "...");
        return "token";
    }
}
