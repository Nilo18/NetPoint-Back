package com.netpoint.main.models;


import com.netpoint.main.filters.DefaultProductAttribute;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    // ეს წინასწარ ანიშნებს რომ დეფაულტ ატრიბუტია, შემდეგ რომ ყველა დეფაულტ ატრიბუტი ერთად მარტივად ამოვიღოთ
    @DefaultProductAttribute(name = "Name", type = ProductAttribute.AttributeType.TEXT)
    private String name;

    @Column(columnDefinition = "TEXT")
    @DefaultProductAttribute(name = "Product Image (Optional)", type = ProductAttribute.AttributeType.TEXT)
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    @DefaultProductAttribute(name = "Retail Price", type = ProductAttribute.AttributeType.TEXT)
    private BigDecimal price;

    @Column(nullable = false)
    @DefaultProductAttribute(name = "Stock Quantity", type = ProductAttribute.AttributeType.TEXT)
    private Integer stock = 0;

    @Column(precision = 10, scale = 2)
    @DefaultProductAttribute(name = "Wholesale Price", type = ProductAttribute.AttributeType.TEXT)
    private BigDecimal wholesalePrice;

    @Column(precision = 7, scale = 2)
    private BigDecimal marginPercent;

    @Transient
    public BigDecimal getProfitability() {
        if (wholesalePrice == null || price == null) return null;
        return price.subtract(wholesalePrice);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    private Company company;

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();
}
