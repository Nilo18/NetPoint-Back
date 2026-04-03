package com.netpoint.main.controllers;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.requests.VerifyOtpRequest;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netpoint.main.dto.responses.AuthResponse;
@RestController
@RequestMapping(path="/auth")
public class AuthController {

    // ეს authService უმკლავდება ყველა ავთენტიკაციის ლოგიკას
    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/verify-2fa
    // ეს მეორე ნაბიჯია რა owner/admin ლოგ ინის — OTP ვალიდირებას აკეთებს და აბრუნებს JWT თ იმუშავა
    @PostMapping(path="/verify-2fa")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(this.authService.verifyOtp(request));
    }

    // POST /auth/login
    // ესაა პირველი ნაბიჯი რა საიტზე შესვლის
    // ქეშიერები JWT-ს პირდაპირ იღებენ და Admin/Owner დროებით ტოკენს-tempToken იღებენ და ესემესი მისდით(წესით მაგრამ როგორც გითხარი მარტო ჩემს ნომერზე მოდის)
    @PostMapping(path="/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(this.authService.login(request));
    }

    @PostMapping(path="/signup")
    public ResponseEntity<CompanySignupResponse> signup(@Valid @RequestBody CompanyRegistrationRequest company) {
        CompanySignupResponse created = this.authService.signup(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
