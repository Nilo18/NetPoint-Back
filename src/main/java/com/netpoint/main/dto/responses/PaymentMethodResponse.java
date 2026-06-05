package com.netpoint.main.dto.responses;

public record PaymentMethodResponse(
        Integer id,
        String  mockPaymentMethodId,
        String  cardBrand,
        String  cardLast4,
        Short   cardExpMonth,
        Short   cardExpYear,
        String  cardholderName,
        Boolean isDefault,
        String  status
) {}