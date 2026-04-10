package com.delivera.service;

import com.delivera.security.SecurityUtils;
import com.delivera.dto.loyaluser.LoyalUserRequest;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.LoyalUserConflictException;
import com.delivera.exception.OrderNotFoundException;
import com.delivera.model.*;
import com.delivera.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyalUserServiceTest {

    @Mock
    private LoyalUserRepository loyalUserRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private LoyalUserService loyalUserService;

    private UUID companyId;
    private Company company;
    private LoyalUser loyalUser;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);

        loyalUser = new LoyalUser();
        loyalUser.setId(UUID.randomUUID());
        loyalUser.setEmail("loyal@test.com");
        loyalUser.getCompanies().add(company);
    }

    @Test
    void getByCompany_returnsUsersWithOrders() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompaniesIdOrderByCreatedAtDesc(companyId)).thenReturn(List.of(loyalUser));
        when(orderRepository.countByLoyalUserId(loyalUser.getId())).thenReturn(2L);

        assertThat(loyalUserService.getByCompany()).hasSize(1);
    }

    @Test
    void add_success_noRegisteredUser() {
        LoyalUserRequest req = new LoyalUserRequest("new@test.com");
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompaniesIdAndEmail(companyId, "new@test.com")).thenReturn(Optional.empty());
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(loyalUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = loyalUserService.add(req);
        assertThat(result.email()).isEqualTo("new@test.com");
        verify(loyalUserRepository).save(any());
    }

    @Test
    void getMyOrders_returnsOrdersByRecipientEmail() {
        when(securityUtils.getCurrentEmail()).thenReturn("loyal@test.com");
        when(orderRepository.findByRecipientEmailOrderByCreatedAtDesc("loyal@test.com")).thenReturn(List.of());

        assertThat(loyalUserService.getMyOrders()).isEmpty();
        verify(orderRepository).findByRecipientEmailOrderByCreatedAtDesc("loyal@test.com");
    }
}
