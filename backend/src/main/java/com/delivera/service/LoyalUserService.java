package com.delivera.service;

import com.delivera.config.SecurityUtils;
import com.delivera.dto.loyaluser.LoyalUserRequest;
import com.delivera.dto.loyaluser.LoyalUserResponse;
import com.delivera.dto.order.OrderResponse;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.LoyalUserConflictException;
import com.delivera.exception.OrderNotFoundException;
import com.delivera.model.LoyalUser;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.LoyalUserRepository;
import com.delivera.repository.OrderRepository;
import com.delivera.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LoyalUserService {

    private final LoyalUserRepository loyalUserRepository;
    private final OrderRepository orderRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public LoyalUserService(LoyalUserRepository loyalUserRepository,
                            OrderRepository orderRepository,
                            CompanyRepository companyRepository,
                            UserRepository userRepository,
                            SecurityUtils securityUtils) {
        this.loyalUserRepository = loyalUserRepository;
        this.orderRepository = orderRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public List<LoyalUserResponse> getByCompany() {
        UUID companyId = securityUtils.getCurrentCompanyId();
        return loyalUserRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream().map(LoyalUserResponse::from).toList();
    }

    @Transactional
    public LoyalUserResponse add(LoyalUserRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        String email = request.email().toLowerCase().trim();

        if (loyalUserRepository.findByCompanyIdAndEmail(companyId, email).isPresent()) {
            throw new LoyalUserConflictException();
        }

        var company = companyRepository.findById(companyId)
                .orElseThrow(CompanyContextException::new);

        var lu = new LoyalUser();
        lu.setCompany(company);
        lu.setEmail(email);
        userRepository.findByEmail(email).ifPresent(lu::setUser);

        return LoyalUserResponse.from(loyalUserRepository.save(lu));
    }

    public List<OrderResponse> getOrdersForLoyalUser(UUID loyalUserId) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        loyalUserRepository.findByIdAndCompanyId(loyalUserId, companyId)
                .orElseThrow(OrderNotFoundException::new);
        return orderRepository.findByLoyalUserIdOrderByCreatedAtDesc(loyalUserId)
                .stream().map(OrderResponse::from).toList();
    }

    /** Endpoint para el propio usuario fidelizado: obtiene sus pedidos de todas las empresas */
    public List<OrderResponse> getMyOrders() {
        String email = securityUtils.getCurrentEmail();
        return loyalUserRepository.findByEmail(email).stream()
                .flatMap(lu -> orderRepository.findByLoyalUserIdOrderByCreatedAtDesc(lu.getId()).stream())
                .map(OrderResponse::from)
                .toList();
    }
}
