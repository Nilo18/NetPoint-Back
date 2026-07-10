package com.netpoint.main.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull
    @Min(value = 1)
    @Max(value = 1000000)
    private Integer stock;

    @NotNull
    @Min(value = 0)
    @Max(value = 100000000)
    private BigDecimal wholesalePrice;

    private String imageUrl;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Min(value = 0)
    @Max(value = 100000000)
    private BigDecimal retailPrice;

    private Map<String, JsonNode> customAttributes;
}
