package com.netpoint.main.models;

import lombok.Data;

@Data
public class Company {
    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final String industry;
}
