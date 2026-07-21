package com.netpoint.main.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 120)
    private String productNameSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_retail_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitRetailPrice;

    @Column(name = "unit_wholesale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitWholesalePrice;

    @Column(name = "line_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineRevenue;

    @Column(name = "line_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineCost;

    @Column(name = "line_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineProfit;
}