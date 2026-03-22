package com.delivera.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_priority_config")
public class OrderPriorityConfig {

    @Id
    private String priority;

    @Column(name = "ui_severity", nullable = false, length = 20)
    private String uiSeverity;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getPriority() { return priority; }
    public String getUiSeverity() { return uiSeverity; }
    public int getSortOrder() { return sortOrder; }
}
