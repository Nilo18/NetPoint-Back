package com.netpoint.main.dto.requests;



import com.netpoint.main.models.ProductAttribute.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductAttributeRequest {

    @NotBlank(message = "Attribute name is required")
    private String attributeName;

    @NotNull(message = "Attribute type is required")
    private AttributeType attributeType;
}