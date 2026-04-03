package com.netpoint.main.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

public record AuthResponse(String status, String token) {
}