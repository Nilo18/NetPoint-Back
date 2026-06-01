package com.netpoint.main.dto;



import java.math.BigDecimal;
import java.util.Map;

public record ProductDTO(
        Integer id,
        String name,
        BigDecimal price,
        Map<String, String> customAttributes,
        Integer stock,
        BigDecimal wholesalePrice,
        BigDecimal marginPercent,
        BigDecimal profitability,
        String imageUrl
) {}