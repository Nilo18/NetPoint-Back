package com.netpoint.main.dto;

public record AuthenticatedUser(String userId, String role, Integer companyId) {
}
