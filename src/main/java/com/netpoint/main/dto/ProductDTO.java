package com.netpoint.main.dto;



import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Map;

public record ProductDTO(
        Integer id,
        String name,
        BigDecimal retailPrice,
        Map<String, JsonNode> customAttributes,
        Integer stock,
        BigDecimal wholesalePrice,
        BigDecimal marginPercent,
        BigDecimal profitability,
        String imageUrl
) {}