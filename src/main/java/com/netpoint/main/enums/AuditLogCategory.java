package com.netpoint.main.enums;

import com.netpoint.main.models.AuditLog;

public enum AuditLogCategory {
    SALES,
    PRODUCT_CHANGES,
    TEAM_CHANGES,
    COMPANY_CHANGES,
    PAYMENT_CHANGES,
    ACCOUNT_CHANGES;

    public static AuditLogCategory from(AuditLog.EventType eventType) {
        return switch (eventType) {
            case SALE_COMPLETED -> SALES;
            case PRODUCT_ADDED, PRODUCT_UPDATED, PRODUCT_DELETED -> PRODUCT_CHANGES;
            case USER_INVITED, TEAM_MEMBER_ADDED, TEAM_MEMBER_REMOVED -> TEAM_CHANGES;
            case COMPANY_DELETED, COMPANY_INFO_UPDATED -> COMPANY_CHANGES;
            case PAYMENT_METHOD_ADDED, PAYMENT_METHOD_UPDATED, PAYMENT_METHOD_REMOVED,
                 PAYMENT_PLAN_CHANGED, SUBSCRIPTION_CANCELLED -> PAYMENT_CHANGES;
            case ACCOUNT_INFO_UPDATED -> ACCOUNT_CHANGES;
        };
    }
}
