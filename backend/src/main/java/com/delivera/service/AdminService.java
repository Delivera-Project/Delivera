package com.delivera.service;

import com.delivera.dto.admin.GlobalMetrics;
import com.delivera.dto.admin.OrganizationSummary;
import com.delivera.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class AdminService {

    private final OrganizationRepository organizationRepository;
    private final CompanyRepository companyRepository;
    private final WorkerRepository workerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminService(OrganizationRepository organizationRepository,
                        CompanyRepository companyRepository,
                        WorkerRepository workerRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.companyRepository = companyRepository;
        this.workerRepository = workerRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<OrganizationSummary> listOrganizations() {
        return organizationRepository.findAll().stream()
                .map(org -> new OrganizationSummary(
                        org.getId(),
                        org.getName(),
                        org.getHandle(),
                        org.getCreatedAt(),
                        companyRepository.countByOrganizationId(org.getId()),
                        workerRepository.countByOrganizationId(org.getId()),
                        orderRepository.countByOrganizationId(org.getId())))
                .toList();
    }

    public GlobalMetrics getGlobalMetrics() {
        Instant monthStart = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        return new GlobalMetrics(
                organizationRepository.count(),
                companyRepository.count(),
                orderRepository.countByCreatedAtAfter(monthStart),
                userRepository.count());
    }
}
