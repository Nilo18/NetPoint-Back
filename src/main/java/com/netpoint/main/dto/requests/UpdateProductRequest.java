package com.netpoint.main.dto.requests;



import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    @NotNull
    @Min(value = 1)
    @Max(value = 1000000)
    private Integer stock;
    @Min(value = 0)
    @Max(value = 100000000)
    @NotNull
    private BigDecimal wholesalePrice;
    private String imageUrl;
    @Positive(message = "Price must be positive")
    @NotNull(message = "Price is required")
    @Min(value = 1)
    @Max(value = 100000000)
    private BigDecimal retailPrice;

    private Map<String, JsonNode> customAttributes;
}
