package com.delivera.dto.config;

import java.util.List;

public record AppConfigResponse(
        List<OrderStatusConfigDto> orderStatuses,
        List<OrderPriorityConfigDto> orderPriorities,
        List<WorkerRoleConfigDto> roleCapabilities
) {}
