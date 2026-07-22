package com.netpoint.main.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private EventType eventType;

    @Column(length = 500)
    private String details;

    @Column(name = "actor_name_snapshot")
    private String actorNameSnapshot;

    @Column(name = "actor_role_snapshot")
    private String actorRoleSnapshot;

    @Column(name = "company_name_snapshot")
    private String companyNameSnapshot;

    private LocalDateTime occurredAt;

    public enum EventType {
        SALE_COMPLETED,
        PRODUCT_ADDED,
        PRODUCT_DELETED,
        USER_INVITED,
        TEAM_MEMBER_ADDED,
        TEAM_MEMBER_REMOVED,
        COMPANY_DELETED,
        PAYMENT_METHOD_ADDED,
        PAYMENT_METHOD_UPDATED,
        PAYMENT_METHOD_REMOVED,
        PAYMENT_PLAN_CHANGED,
        SUBSCRIPTION_CANCELLED,
        ACCOUNT_INFO_UPDATED,
        COMPANY_INFO_UPDATED;

        public static boolean contains(String test) {
            if (test == null) return false;

            return Arrays.stream(EventType.values())
                    .anyMatch(event -> event.name().equalsIgnoreCase(test));
        }
    }
}
