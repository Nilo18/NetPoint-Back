package com.netpoint.main.exceptions;

public class PaymentPlanNotFoundException extends RuntimeException {
    public PaymentPlanNotFoundException(String message) {
        super(message);
    }
}
