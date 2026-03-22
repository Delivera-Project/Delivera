package com.delivera.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusConfigTest {

    private OrderStatusConfig configWithTransitions(String value) throws Exception {
        OrderStatusConfig config = new OrderStatusConfig();
        Field field = OrderStatusConfig.class.getDeclaredField("allowedTransitions");
        field.setAccessible(true);
        field.set(config, value);
        return config;
    }

    @Test
    void getAllowedTransitionsList_returnsMultiple() throws Exception {
        OrderStatusConfig config = configWithTransitions("IN_TRANSIT,CANCELLED");
        assertThat(config.getAllowedTransitionsList()).containsExactly("IN_TRANSIT", "CANCELLED");
    }

    @Test
    void getAllowedTransitionsList_returnsSingle() throws Exception {
        OrderStatusConfig config = configWithTransitions("DELIVERED");
        assertThat(config.getAllowedTransitionsList()).containsExactly("DELIVERED");
    }

    @Test
    void getAllowedTransitionsList_nullReturnsEmpty() throws Exception {
        OrderStatusConfig config = configWithTransitions(null);
        assertThat(config.getAllowedTransitionsList()).isEmpty();
    }

    @Test
    void getAllowedTransitionsList_blankReturnsEmpty() throws Exception {
        OrderStatusConfig config = configWithTransitions("   ");
        assertThat(config.getAllowedTransitionsList()).isEmpty();
    }

    @Test
    void getAllowedTransitionsList_emptyStringReturnsEmpty() throws Exception {
        OrderStatusConfig config = configWithTransitions("");
        assertThat(config.getAllowedTransitionsList()).isEmpty();
    }

    @Test
    void getters_returnSetValues() throws Exception {
        OrderStatusConfig config = new OrderStatusConfig();
        Field status = OrderStatusConfig.class.getDeclaredField("status");
        Field uiSeverity = OrderStatusConfig.class.getDeclaredField("uiSeverity");
        Field terminal = OrderStatusConfig.class.getDeclaredField("terminal");
        Field sortOrder = OrderStatusConfig.class.getDeclaredField("sortOrder");
        status.setAccessible(true); uiSeverity.setAccessible(true);
        terminal.setAccessible(true); sortOrder.setAccessible(true);
        status.set(config, "PENDING"); uiSeverity.set(config, "warn");
        terminal.set(config, false); sortOrder.set(config, 1);

        assertThat(config.getStatus()).isEqualTo("PENDING");
        assertThat(config.getUiSeverity()).isEqualTo("warn");
        assertThat(config.isTerminal()).isFalse();
        assertThat(config.getSortOrder()).isEqualTo(1);
    }

    @Test
    void getAllowedTransitionsList_returnsListType() throws Exception {
        OrderStatusConfig config = configWithTransitions("A,B");
        assertThat(config.getAllowedTransitionsList()).isInstanceOf(List.class);
    }
}
