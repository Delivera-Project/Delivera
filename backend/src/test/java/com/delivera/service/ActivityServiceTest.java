package com.delivera.service;

import com.delivera.model.OrderStatus;
import com.delivera.repository.LoyalUserRepository;
import com.delivera.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private LoyalUserRepository loyalUserRepository;
    @InjectMocks private ActivityService activityService;

    @Test
    void getMetrics_month_returnsCorrectCounts() {
        UUID companyId = UUID.randomUUID();
        when(orderRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(10L);
        when(orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(eq(companyId), eq(OrderStatus.DELIVERED), any(Instant.class))).thenReturn(6L);
        when(orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(eq(companyId), eq(OrderStatus.CANCELLED), any(Instant.class))).thenReturn(1L);
        when(orderRepository.countByCompanyIdAndStatusNotIn(eq(companyId), any(Collection.class))).thenReturn(3L);
        when(loyalUserRepository.countByCompaniesIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(2L);

        var result = activityService.getMetrics(companyId, "MONTH");

        assertThat(result.period()).isEqualTo("MONTH");
        assertThat(result.totalOrders()).isEqualTo(10);
        assertThat(result.completedOrders()).isEqualTo(6);
        assertThat(result.cancelledOrders()).isEqualTo(1);
        assertThat(result.activeOrders()).isEqualTo(3);
        assertThat(result.newLoyalUsers()).isEqualTo(2);
    }

    @Test
    void getMetrics_today_usesTodayPeriod() {
        UUID companyId = UUID.randomUUID();
        when(orderRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(0L);
        when(orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(any(), any(), any(Instant.class))).thenReturn(0L);
        when(orderRepository.countByCompanyIdAndStatusNotIn(any(), any())).thenReturn(0L);
        when(loyalUserRepository.countByCompaniesIdAndCreatedAtAfter(any(), any(Instant.class))).thenReturn(0L);

        var result = activityService.getMetrics(companyId, "TODAY");
        assertThat(result.period()).isEqualTo("TODAY");
    }

    @Test
    void getMetrics_week_usesWeekPeriod() {
        UUID companyId = UUID.randomUUID();
        when(orderRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(5L);
        when(orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(any(), any(), any(Instant.class))).thenReturn(0L);
        when(orderRepository.countByCompanyIdAndStatusNotIn(any(), any())).thenReturn(5L);
        when(loyalUserRepository.countByCompaniesIdAndCreatedAtAfter(any(), any(Instant.class))).thenReturn(0L);

        var result = activityService.getMetrics(companyId, "WEEK");
        assertThat(result.period()).isEqualTo("WEEK");
        assertThat(result.totalOrders()).isEqualTo(5);
    }
}
