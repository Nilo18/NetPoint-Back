package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CheckoutRequest(
        @NotEmpty
        List<CheckoutRequestItem> items
) {
}
