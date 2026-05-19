package com.netpoint.main.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private String cardType; // "VISA", "MASTERCARD"
    private String lastFourDigits;
    private String expiryDate; // "12/26"
}
