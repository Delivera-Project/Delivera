package com.delivera.service;

import com.delivera.dto.config.*;
import com.delivera.exception.InvalidStatusTransitionException;
import com.delivera.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppConfigService {

    @Autowired private OrderStatusConfigRepository statusConfigRepository;
    @Autowired private OrderPriorityConfigRepository priorityConfigRepository;
    @Autowired private WorkerRoleConfigRepository roleConfigRepository;

    public AppConfigResponse getConfig() {
        List<OrderStatusConfigDto> statuses = statusConfigRepository.findAllByOrderBySortOrderAsc()
                .stream().map(OrderStatusConfigDto::from).toList();
        List<OrderPriorityConfigDto> priorities = priorityConfigRepository.findAllByOrderBySortOrderAsc()
                .stream().map(OrderPriorityConfigDto::from).toList();
        List<WorkerRoleConfigDto> roles = roleConfigRepository.findAll()
                .stream().map(WorkerRoleConfigDto::from).toList();
        return new AppConfigResponse(statuses, priorities, roles);
    }

    public void validateTransition(String current, String next) {
        statusConfigRepository.findById(current).ifPresentOrElse(config -> {
            if (config.isTerminal() || !config.getAllowedTransitionsList().contains(next)) {
                throw new InvalidStatusTransitionException();
            }
        }, () -> { throw new InvalidStatusTransitionException(); });
    }
}
