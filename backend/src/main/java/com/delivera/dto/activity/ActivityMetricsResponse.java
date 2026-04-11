package com.delivera.dto.activity;

public record ActivityMetricsResponse(
        String period,
        long totalOrders,
        long completedOrders,
        long cancelledOrders,
        long activeOrders,
        long newLoyalUsers
) {}
