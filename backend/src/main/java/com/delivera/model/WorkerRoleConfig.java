package com.delivera.model;

import jakarta.persistence.*;

@Entity
@Table(name = "worker_role_config")
public class WorkerRoleConfig {

    @Id
    private String role;

    @Column(name = "can_create_orders", nullable = false)
    private boolean canCreateOrders;

    @Column(name = "can_update_order_status", nullable = false)
    private boolean canUpdateOrderStatus;

    @Column(name = "can_manage_units", nullable = false)
    private boolean canManageUnits;

    @Column(name = "can_manage_loyal_users", nullable = false)
    private boolean canManageLoyalUsers;

    @Column(name = "can_manage_settings", nullable = false)
    private boolean canManageSettings;

    public String getRole() { return role; }
    public boolean isCanCreateOrders() { return canCreateOrders; }
    public boolean isCanUpdateOrderStatus() { return canUpdateOrderStatus; }
    public boolean isCanManageUnits() { return canManageUnits; }
    public boolean isCanManageLoyalUsers() { return canManageLoyalUsers; }
    public boolean isCanManageSettings() { return canManageSettings; }
}
