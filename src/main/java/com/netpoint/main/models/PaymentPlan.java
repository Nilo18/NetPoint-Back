package com.netpoint.main.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "payment_plans")
public class PaymentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String planName;
    private String planPurpose;
    private Double costPerMonth;
    // Tells JPA to create a separate table 'payment_plan_rules' automatically
    @ElementCollection
    @CollectionTable(name = "payment_plan_rules", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "rule")
    private List<String> planRules;
}
