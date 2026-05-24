package com.netpoint.main.dto.requests;



import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private Map<String, String> customAttributes;
}