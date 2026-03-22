package com.delivera.service;

import com.delivera.exception.InvalidStatusTransitionException;
import com.delivera.model.OrderPriorityConfig;
import com.delivera.model.OrderStatusConfig;
import com.delivera.model.WorkerRoleConfig;
import com.delivera.repository.OrderPriorityConfigRepository;
import com.delivera.repository.OrderStatusConfigRepository;
import com.delivera.repository.WorkerRoleConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    @Mock private OrderStatusConfigRepository statusConfigRepository;
    @Mock private OrderPriorityConfigRepository priorityConfigRepository;
    @Mock private WorkerRoleConfigRepository roleConfigRepository;
    @InjectMocks private AppConfigService appConfigService;

    private OrderStatusConfig pendingConfig;

    @BeforeEach
    void setUp() throws Exception {
        pendingConfig = statusConfig("PENDING", "warn", "IN_TRANSIT,CANCELLED", false, 1);
    }

    @Test
    void getConfig_returnsResponse() {
        when(statusConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(pendingConfig));
        when(priorityConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(roleConfigRepository.findAll()).thenReturn(List.of());

        var result = appConfigService.getConfig();
        assertThat(result.orderStatuses()).hasSize(1);
        assertThat(result.orderPriorities()).isEmpty();
        assertThat(result.roleCapabilities()).isEmpty();
    }

    @Test
    void validateTransition_validTransition_doesNotThrow() {
        when(statusConfigRepository.findById("PENDING")).thenReturn(Optional.of(pendingConfig));
        appConfigService.validateTransition("PENDING", "IN_TRANSIT"); // should not throw
    }

    @Test
    void validateTransition_terminalStatus_throws() throws Exception {
        OrderStatusConfig delivered = statusConfig("DELIVERED", "success", "", true, 3);
        when(statusConfigRepository.findById("DELIVERED")).thenReturn(Optional.of(delivered));
        assertThatThrownBy(() -> appConfigService.validateTransition("DELIVERED", "PENDING"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void validateTransition_transitionNotAllowed_throws() {
        when(statusConfigRepository.findById("PENDING")).thenReturn(Optional.of(pendingConfig));
        assertThatThrownBy(() -> appConfigService.validateTransition("PENDING", "DELIVERED"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void validateTransition_statusNotFound_throws() {
        when(statusConfigRepository.findById("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appConfigService.validateTransition("UNKNOWN", "PENDING"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void getConfig_mapsAllConfigTypes() throws Exception {
        OrderPriorityConfig prio = new OrderPriorityConfig();
        setPriorityField(prio, "priority", "HIGH");
        setPriorityField(prio, "uiSeverity", "danger");
        setPriorityField(prio, "sortOrder", 1);

        WorkerRoleConfig role = new WorkerRoleConfig();
        setRoleField(role, "role", "COMPANY_ADMIN");

        when(statusConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(pendingConfig));
        when(priorityConfigRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(prio));
        when(roleConfigRepository.findAll()).thenReturn(List.of(role));

        var result = appConfigService.getConfig();
        assertThat(result.orderStatuses()).hasSize(1);
        assertThat(result.orderPriorities()).hasSize(1);
        assertThat(result.roleCapabilities()).hasSize(1);
    }

    private OrderStatusConfig statusConfig(String status, String severity, String transitions,
                                            boolean terminal, int sortOrder) throws Exception {
        OrderStatusConfig config = new OrderStatusConfig();
        setField(config, "status", status);
        setField(config, "uiSeverity", severity);
        setField(config, "allowedTransitions", transitions);
        setField(config, "terminal", terminal);
        setField(config, "sortOrder", sortOrder);
        return config;
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private void setPriorityField(OrderPriorityConfig obj, String name, Object value) throws Exception {
        Field f = OrderPriorityConfig.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private void setRoleField(WorkerRoleConfig obj, String name, Object value) throws Exception {
        Field f = WorkerRoleConfig.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }
}
