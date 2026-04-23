package com.netpoint.main.dto.requests;

public record CompleteRegistrationRequest(
        String token,
        String password,
        String fullName
    ) {
}