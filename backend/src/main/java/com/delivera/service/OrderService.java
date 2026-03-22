package com.delivera.service;

import com.delivera.config.SecurityUtils;
import com.delivera.dto.order.*;
import com.delivera.exception.*;
import com.delivera.model.*;
import com.delivera.repository.*;
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
    private final LoyalUserRepository loyalUserRepository;
    private final SecurityUtils securityUtils;
    private final AppConfigService appConfigService;

    public OrderService(OrderRepository orderRepository,
                        OperationalUnitRepository unitRepository,
                        CompanyRepository companyRepository,
                        LoyalUserRepository loyalUserRepository,
                        SecurityUtils securityUtils,
                        AppConfigService appConfigService) {
        this.orderRepository = orderRepository;
        this.unitRepository = unitRepository;
        this.companyRepository = companyRepository;
        this.loyalUserRepository = loyalUserRepository;
        this.securityUtils = securityUtils;
        this.appConfigService = appConfigService;
    }

    public List<OrderResponse> getByCompany() {
        UUID companyId = securityUtils.getCurrentCompanyId();
        return orderRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderDetailResponse getDetail(UUID id) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        Order order = orderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(OrderNotFoundException::new);
        return OrderDetailResponse.from(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();

        boolean isExternal = request.destinationId() == null;

        var origin = unitRepository.findByIdAndCompanyId(request.originId(), companyId)
                .orElseThrow(InvalidOrderUnitsException::new);

        OperationalUnit destination = null;
        if (!isExternal) {
            destination = unitRepository.findByIdAndCompanyId(request.destinationId(), companyId)
                    .orElseThrow(InvalidOrderUnitsException::new);
            if (origin.getId().equals(destination.getId())) {
                throw new InvalidOrderUnitsException();
            }
        }

        var company = companyRepository.findById(companyId)
                .orElseThrow(CompanyContextException::new);

        var order = new Order();
        order.setCompany(company);
        order.setReference(generateReference());
        order.setOrigin(origin);
        order.setDestination(destination);
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(request.priority() != null ? request.priority() : OrderPriority.NORMAL);
        order.setNotes(request.notes() != null ? request.notes().trim() : null);

        if (isExternal && request.recipientEmail() != null) {
            String recipientEmail = request.recipientEmail().toLowerCase().trim();
            order.setRecipientEmail(recipientEmail);
            order.setRecipientName(request.recipientName() != null ? request.recipientName().trim() : null);
            order.setTrackingToken(UUID.randomUUID().toString().replace("-", ""));
            loyalUserRepository.findByCompanyIdAndEmail(companyId, recipientEmail)
                    .ifPresent(order::setLoyalUser);
        }

        var initialEvent = new OrderEvent();
        initialEvent.setOrder(order);
        initialEvent.setStatus(OrderStatus.PENDING);
        initialEvent.setAuthorEmail(securityUtils.getCurrentEmail());
        order.getEvents().add(initialEvent);

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderDetailResponse updateStatus(UUID id, OrderStatusRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        String email = securityUtils.getCurrentEmail();

        Order order = orderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(OrderNotFoundException::new);

        validateTransition(order.getStatus(), request.status());

        order.setStatus(request.status());

        var event = new OrderEvent();
        event.setOrder(order);
        event.setStatus(request.status());
        event.setNote(request.note() != null ? request.note().trim() : null);
        event.setAuthorEmail(email);
        order.getEvents().add(event);

        return OrderDetailResponse.from(orderRepository.save(order));
    }

    @Transactional
    public void delete(UUID id) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        Order order = orderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(OrderNotFoundException::new);
        orderRepository.delete(order);
    }

    public PublicOrderResponse getPublicByToken(String token) {
        Order order = orderRepository.findByTrackingToken(token)
                .orElseThrow(OrderNotFoundException::new);
        return PublicOrderResponse.from(order);
    }

    public PublicOrderResponse getPublicByReference(String reference) {
        Order order = orderRepository.findByReference(reference.toUpperCase().trim())
                .orElseThrow(OrderNotFoundException::new);
        return PublicOrderResponse.from(order);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        appConfigService.validateTransition(current.name(), next.name());
    }

    private String generateReference() {
        long seq = orderRepository.nextReferenceSeq();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("DEL-%s-%04d", date, seq);
    }
}
