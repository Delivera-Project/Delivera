package com.delivera.model;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "order_status_config")
public class OrderStatusConfig {

    @Id
    private String status;

    @Column(name = "ui_severity", nullable = false, length = 20)
    private String uiSeverity;

    @Column(name = "allowed_transitions", nullable = false, length = 500)
    private String allowedTransitions;

    @Column(name = "is_terminal", nullable = false)
    private boolean terminal;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getStatus() { return status; }
    public String getUiSeverity() { return uiSeverity; }
    public boolean isTerminal() { return terminal; }
    public int getSortOrder() { return sortOrder; }

    public List<String> getAllowedTransitionsList() {
        if (allowedTransitions == null || allowedTransitions.isBlank()) return List.of();
        return Arrays.asList(allowedTransitions.split(","));
    }
}
