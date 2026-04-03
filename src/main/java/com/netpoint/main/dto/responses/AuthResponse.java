package com.netpoint.main.dto.responses;

public class AuthResponse {

    // DTO აბრუნებს ორივე /auth/login და /auth/verify-2fa
// status: "authenticated" (JWT გაცემულია) ან "2fa_required" (OTP გამოიგზავნება ესემესით)
// token გაძლევს: JWT თ ავტეტიკირებული, tempToken თუ 2fa_required ანუ 2ფაქტორ თ უნდა
    private String status;
    private String token;

    public AuthResponse(String status, String token) {
        this.status = status;
        this.token = token;
    }

    public String getStatus() { return status; }
    public String getToken() { return token; }
}