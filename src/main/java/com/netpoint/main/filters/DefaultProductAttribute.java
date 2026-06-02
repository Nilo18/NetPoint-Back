package com.netpoint.main.filters;

import com.netpoint.main.models.ProductAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultProductAttribute {
    String name() default "";
    ProductAttribute.AttributeType type();
}
