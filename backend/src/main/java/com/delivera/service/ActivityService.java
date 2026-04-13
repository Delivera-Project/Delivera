package com.delivera.service;

import com.delivera.dto.activity.ActivityMetricsResponse;
import com.delivera.dto.activity.OrdersByDayEntry;
import com.delivera.model.OrderStatus;
import com.delivera.repository.LoyalUserRepository;
import com.delivera.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private static final List<OrderStatus> TERMINAL = List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private final OrderRepository orderRepository;
    private final LoyalUserRepository loyalUserRepository;

    public ActivityService(OrderRepository orderRepository, LoyalUserRepository loyalUserRepository) {
        this.orderRepository = orderRepository;
        this.loyalUserRepository = loyalUserRepository;
    }

    public ActivityMetricsResponse getMetrics(UUID companyId, String period) {
        Instant from = periodStart(period);
        return new ActivityMetricsResponse(
                period,
                orderRepository.countByCompanyIdAndCreatedAtAfter(companyId, from),
                orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(companyId, OrderStatus.DELIVERED, from),
                orderRepository.countByCompanyIdAndStatusAndCreatedAtAfter(companyId, OrderStatus.CANCELLED, from),
                orderRepository.countByCompanyIdAndStatusNotIn(companyId, TERMINAL),
                loyalUserRepository.countByCompaniesIdAndCreatedAtAfter(companyId, from)
        );
    }

    public List<OrdersByDayEntry> getOrdersByDay(UUID companyId, String period) {
        Instant from = periodStart(period);
        return orderRepository.countByDayForCompany(companyId, from).stream()
                .map(row -> new OrdersByDayEntry((LocalDate) row[0], (long) row[1]))
                .toList();
    }

    Instant periodStart(String period) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return switch (period) {
            case "TODAY" -> today.atStartOfDay().toInstant(ZoneOffset.UTC);
            case "WEEK"  -> today.with(DayOfWeek.MONDAY).atStartOfDay().toInstant(ZoneOffset.UTC);
            default      -> today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay().toInstant(ZoneOffset.UTC);
        };
    }
}
