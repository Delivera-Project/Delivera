package com.delivera.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_companies", nullable = false)
    private int maxCompanies;

    @Column(name = "max_units", nullable = false)
    private int maxUnits;

    @Column(name = "max_workers", nullable = false)
    private int maxWorkers;

    @Column(name = "max_orders_per_month", nullable = false)
    private int maxOrdersPerMonth;

    @Column(name = "max_loyal_users", nullable = false)
    private int maxLoyalUsers;

    /** Returns true if the resource can be created (unlimited if max == -1). */
    public boolean allows(long current, int max) {
        return max == -1 || current < max;
    }
}
