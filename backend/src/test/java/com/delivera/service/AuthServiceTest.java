package com.delivera.service;

import com.delivera.dto.auth.*;
import com.delivera.exception.*;
import com.delivera.model.*;
import com.delivera.repository.*;
import com.delivera.repository.ActivityTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private LoyalUserRepository loyalUserRepository;
    @Mock
    private ActivityTypeRepository activityTypeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    private User user;
    private Company company;
    private Organization organization;
    private Worker worker;
    private Order claimOrder;
    private ClaimRegisterRequest claimRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("admin@test.com");
        user.setPasswordHash("hashed");

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("TestOrg");
        organization.setHandle("test-org");

        company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("TestCompany");
        company.setOrganization(organization);

        worker = new Worker();
        worker.setUser(user);
        worker.setCompany(company);
        worker.setRole(WorkerRole.COMPANY_ADMIN);

        claimOrder = new Order();
        claimOrder.setTrackingToken("testtoken");
        claimOrder.setRecipientEmail("juan@gmail.com");
        claimOrder.setCompany(company);

        claimRequest = new ClaimRegisterRequest("Juan", "García", "juan@gmail.com", "Password1");
    }

    // --- login ---

    @Test
    void login_workerUser_returnsTokenWithCompany() {
        when(userRepository.findByEmailOrUsername("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(workerRepository.findByUserEmailOrderByCreatedAtAsc("admin@test.com")).thenReturn(List.of(worker));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("worker-token");

        LoginResponse result = authService.login("admin@test.com", "pass");

        assertThat(result.token()).isEqualTo("worker-token");
        assertThat(result.companyId()).isEqualTo(company.getId());
        assertThat(result.role()).isEqualTo("COMPANY_ADMIN");
    }

    @Test
    void login_individualUser_returnsTokenWithoutCompany() {
        when(userRepository.findByEmailOrUsername("personal@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(workerRepository.findByUserEmailOrderByCreatedAtAsc("admin@test.com")).thenReturn(List.of());
        when(jwtService.generateToken("admin@test.com")).thenReturn("individual-token");

        LoginResponse result = authService.login("personal@test.com", "pass");

        assertThat(result.token()).isEqualTo("individual-token");
        assertThat(result.companyId()).isNull();
    }

    // --- register ---

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("new@test.com", "newuser", "John", null, null, "Password1");
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken("new@test.com")).thenReturn("token");

        RegisterResponse result = authService.register(req);
        assertThat(result.token()).isEqualTo("token");
        assertThat(result.email()).isEqualTo("new@test.com");
    }

    // --- registerCompany ---

    @Test
    void registerCompany_success() {
        CompanyRegisterRequest req = new CompanyRegisterRequest(
                "ceo@test.com", "Password1", "TestOrg", "test-org",
                "TestCompany", "TRANSPORT", "ceouser", "CEO", null, null);
        when(userRepository.findByEmail("ceo@test.com")).thenReturn(Optional.empty());
        when(organizationRepository.existsByHandle("test-org")).thenReturn(false);
        when(userRepository.existsByUsername("ceouser")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);
        when(organizationRepository.saveAndFlush(any())).thenReturn(organization);
        ActivityType transport = new ActivityType(); transport.setCode("TRANSPORT");
        when(activityTypeRepository.getReferenceById("TRANSPORT")).thenReturn(transport);
        when(companyRepository.save(any())).thenReturn(company);
        when(workerRepository.save(any())).thenReturn(worker);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("company-token");

        CompanyRegisterResponse result = authService.registerCompany(req);
        assertThat(result.token()).isEqualTo("company-token");
    }

    // --- claimRegister ---

    @Test
    void claimRegister_success_noExistingLoyalUser() {
        when(orderRepository.findByTrackingToken("testtoken")).thenReturn(Optional.of(claimOrder));
        when(userRepository.findByEmail("juan@gmail.com")).thenReturn(Optional.empty());
        when(loyalUserRepository.findByCompaniesIdAndEmail(company.getId(), "juan@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(loyalUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken("juan@gmail.com")).thenReturn("jwt-token");

        LoginResponse result = authService.claimRegister("testtoken", claimRequest);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("juan@gmail.com");
        assertThat(result.companyId()).isNull();
        verify(loyalUserRepository).save(any(LoyalUser.class));
        verify(orderRepository).save(claimOrder);
    }

    @Test
    void claimRegister_tokenNotFound_throws() {
        when(orderRepository.findByTrackingToken("badtoken")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.claimRegister("badtoken", claimRequest))
                .isInstanceOf(OrderNotFoundException.class);
    }

}
