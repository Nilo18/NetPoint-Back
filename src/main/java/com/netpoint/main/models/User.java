package com.netpoint.main.models;

import lombok.Data;

@Data
public class User {
    private final int companyId; // Which company the user belongs to
    private final int id;
    private final String name;
    private final String email;
    private final String password;
}
