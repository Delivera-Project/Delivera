package com.delivera.service;

import com.delivera.config.SecurityUtils;
import com.delivera.dto.order.OrderRequest;
import com.delivera.dto.order.OrderResponse;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.InvalidOrderUnitsException;
import com.delivera.model.Order;
import com.delivera.model.OrderStatus;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OperationalUnitRepository;
import com.delivera.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OperationalUnitRepository unitRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtils securityUtils;

    public OrderService(OrderRepository orderRepository,
                        OperationalUnitRepository unitRepository,
                        CompanyRepository companyRepository,
                        SecurityUtils securityUtils) {
        this.orderRepository = orderRepository;
        this.unitRepository = unitRepository;
        this.companyRepository = companyRepository;
        this.securityUtils = securityUtils;
    }

    public List<OrderResponse> getByCompany() {
        UUID companyId = securityUtils.getCurrentCompanyId();
        return orderRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();

        var origin = unitRepository.findByIdAndCompanyId(request.originId(), companyId)
                .orElseThrow(InvalidOrderUnitsException::new);
        var destination = unitRepository.findByIdAndCompanyId(request.destinationId(), companyId)
                .orElseThrow(InvalidOrderUnitsException::new);

        if (origin.getId().equals(destination.getId())) {
            throw new InvalidOrderUnitsException();
        }

        var company = companyRepository.findById(companyId)
                .orElseThrow(CompanyContextException::new);

        var order = new Order();
        order.setCompany(company);
        order.setReference(generateReference());
        order.setOrigin(origin);
        order.setDestination(destination);
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.notes() != null ? request.notes().trim() : null);

        return OrderResponse.from(orderRepository.save(order));
    }

    private String generateReference() {
        long seq = orderRepository.nextReferenceSeq();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("DEL-%s-%04d", date, seq);
    }
}
