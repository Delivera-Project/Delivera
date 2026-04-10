package com.delivera.service;

import com.delivera.model.OrderStatusConfig;
import com.delivera.repository.OrderPriorityConfigRepository;
import com.delivera.repository.OrderStatusConfigRepository;
import com.delivera.repository.WorkerRoleConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    @Mock
    private OrderStatusConfigRepository statusConfigRepository;
    @Mock
    private OrderPriorityConfigRepository priorityConfigRepository;
    @Mock
    private WorkerRoleConfigRepository roleConfigRepository;
    @InjectMocks
    private AppConfigService appConfigService;

    private OrderStatusConfig pendingConfig;

    @BeforeEach
    void setUp() {
        pendingConfig = new OrderStatusConfig();
        ReflectionTestUtils.setField(pendingConfig, "status", "PENDING");
        ReflectionTestUtils.setField(pendingConfig, "uiSeverity", "warn");
        ReflectionTestUtils.setField(pendingConfig, "allowedTransitions", "IN_TRANSIT,CANCELLED");
        ReflectionTestUtils.setField(pendingConfig, "terminal", false);
        ReflectionTestUtils.setField(pendingConfig, "sortOrder", 1);
    }

    @Test
    void getConfig_returnsResponse() {
        org.mockito.Mockito.when(statusConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(pendingConfig));
        org.mockito.Mockito.when(priorityConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        org.mockito.Mockito.when(roleConfigRepository.findAll()).thenReturn(List.of());

        var result = appConfigService.getConfig();
        assertThat(result.orderStatuses()).hasSize(1);
        assertThat(result.orderPriorities()).isEmpty();
    }

    @Test
    void validateTransition_validTransition_doesNotThrow() {
        org.mockito.Mockito.when(statusConfigRepository.findById("PENDING")).thenReturn(Optional.of(pendingConfig));
        appConfigService.validateTransition("PENDING", "IN_TRANSIT");
    }
}
