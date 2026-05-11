package com.delivera.dto.settings;

import com.delivera.model.OrderPriority;

import java.util.UUID;

public record CompanySummary(UUID id, String name, String activityType, String logoData, OrderPriority defaultPriority, boolean defaultPriorityLocked) {}
