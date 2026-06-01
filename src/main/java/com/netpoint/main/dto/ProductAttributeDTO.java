package com.netpoint.main.dto;



import com.netpoint.main.models.ProductAttribute.AttributeType;

public record ProductAttributeDTO(Integer id, String attributeName, AttributeType attributeType, boolean isDefault) {
}