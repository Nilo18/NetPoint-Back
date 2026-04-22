package com.netpoint.main.models;

import lombok.Data;

@Data
public class InviteRequest {
    private String email;
    private String role;
    private Integer companyId;
}