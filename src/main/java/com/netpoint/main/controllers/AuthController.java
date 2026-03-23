package com.netpoint.main.controllers;

import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path="/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return this.authService.login(request);
    }

    @PostMapping(path="/signup")
    public String signup() {
        return "Signup works!";
    }
}
