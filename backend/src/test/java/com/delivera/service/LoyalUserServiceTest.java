package com.delivera.service;

import com.delivera.config.SecurityUtils;
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

    @Mock private LoyalUserRepository loyalUserRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityUtils securityUtils;
    @InjectMocks private LoyalUserService loyalUserService;

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
        loyalUser.setCompany(company);
    }

    @Test
    void getByCompany_filtersUsersWithZeroOrders() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)).thenReturn(List.of(loyalUser));
        when(orderRepository.countByLoyalUserId(loyalUser.getId())).thenReturn(0L);

        assertThat(loyalUserService.getByCompany()).isEmpty();
    }

    @Test
    void getByCompany_returnsUsersWithOrders() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)).thenReturn(List.of(loyalUser));
        when(orderRepository.countByLoyalUserId(loyalUser.getId())).thenReturn(2L);

        assertThat(loyalUserService.getByCompany()).hasSize(1);
    }

    @Test
    void add_success_noRegisteredUser() {
        LoyalUserRequest req = new LoyalUserRequest("new@test.com");
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompanyIdAndEmail(companyId, "new@test.com")).thenReturn(Optional.empty());
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(loyalUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = loyalUserService.add(req);
        assertThat(result.email()).isEqualTo("new@test.com");
        verify(loyalUserRepository).save(any());
    }

    @Test
    void add_alreadyExists_throws() {
        LoyalUserRequest req = new LoyalUserRequest("loyal@test.com");
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompanyIdAndEmail(companyId, "loyal@test.com")).thenReturn(Optional.of(loyalUser));

        assertThatThrownBy(() -> loyalUserService.add(req))
                .isInstanceOf(LoyalUserConflictException.class);
    }

    @Test
    void add_companyNotFound_throws() {
        LoyalUserRequest req = new LoyalUserRequest("new@test.com");
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByCompanyIdAndEmail(companyId, "new@test.com")).thenReturn(Optional.empty());
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loyalUserService.add(req))
                .isInstanceOf(CompanyContextException.class);
    }

    @Test
    void getOrdersForLoyalUser_success() {
        UUID loyalUserId = loyalUser.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByIdAndCompanyId(loyalUserId, companyId)).thenReturn(Optional.of(loyalUser));
        when(orderRepository.findByLoyalUserIdOrderByCreatedAtDesc(loyalUserId)).thenReturn(List.of());

        assertThat(loyalUserService.getOrdersForLoyalUser(loyalUserId)).isEmpty();
    }

    @Test
    void getOrdersForLoyalUser_notFound_throws() {
        UUID loyalUserId = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(loyalUserRepository.findByIdAndCompanyId(loyalUserId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loyalUserService.getOrdersForLoyalUser(loyalUserId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getMyOrders_returnsAllOrdersFromAllCompanies() {
        LoyalUser lu2 = new LoyalUser();
        lu2.setId(UUID.randomUUID());
        when(securityUtils.getCurrentEmail()).thenReturn("loyal@test.com");
        when(loyalUserRepository.findByEmail("loyal@test.com")).thenReturn(List.of(loyalUser, lu2));
        when(orderRepository.findByLoyalUserIdOrderByCreatedAtDesc(loyalUser.getId())).thenReturn(List.of());
        when(orderRepository.findByLoyalUserIdOrderByCreatedAtDesc(lu2.getId())).thenReturn(List.of());

        assertThat(loyalUserService.getMyOrders()).isEmpty();
    }
}
