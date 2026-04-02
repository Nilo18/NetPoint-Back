package com.netpoint.main.dto.responses;

public class AuthResponse {
    private String status;
    private String token;

    public AuthResponse(String status, String token) {
        this.status = status;
        this.token = token;
    }

    public String getStatus() { return status; }
    public String getToken() { return token; }
}