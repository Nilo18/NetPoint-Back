package com.netpoint.main.models;

import lombok.Data;

@Data
public class CompleteRegistrationRequest {
    private String token;
    private String password;
    private String fullName;
}