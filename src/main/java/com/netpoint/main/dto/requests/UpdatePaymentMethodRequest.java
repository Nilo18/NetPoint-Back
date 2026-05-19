package com.netpoint.main.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentMethodRequest {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;
}