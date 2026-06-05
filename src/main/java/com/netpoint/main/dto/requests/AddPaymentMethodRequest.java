package com.netpoint.main.dto.requests;

public record AddPaymentMethodRequest(
        String cardNumber,
        Short  expMonth,
        Short  expYear,
        String cvc,
        String cardholderName
) {}