package com.delivera.service;

import com.delivera.dto.auth.*;
import com.delivera.exception.*;
import com.delivera.model.*;
import com.delivera.repository.*;
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

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private WorkerRepository workerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    private User user;
    private Company company;
    private Organization organization;
    private Worker worker;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("admin@test.com");
        user.setPassword("hashed");

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

    @Test
    void login_wrongPassword_throws() {
        when(userRepository.findByEmailOrUsername("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        assertThatThrownBy(() -> authService.login("admin@test.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownUser_throws() {
        when(userRepository.findByEmailOrUsername("nobody@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login("nobody@test.com", "pass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // --- switchCompany ---

    @Test
    void switchCompany_success() {
        UUID targetId = company.getId();
        when(workerRepository.findByUserEmailAndCompanyId("admin@test.com", targetId)).thenReturn(Optional.of(worker));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("switched-token");

        LoginResponse result = authService.switchCompany("admin@test.com", targetId);
        assertThat(result.token()).isEqualTo("switched-token");
    }

    @Test
    void switchCompany_workerNotFound_throws() {
        UUID targetId = UUID.randomUUID();
        when(workerRepository.findByUserEmailAndCompanyId("admin@test.com", targetId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.switchCompany("admin@test.com", targetId))
                .isInstanceOf(InvalidCredentialsException.class);
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

    @Test
    void register_emailTaken_throws() {
        RegisterRequest req = new RegisterRequest("taken@test.com", "user", "John", null, null, "Password1");
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_usernameTaken_throws() {
        RegisterRequest req = new RegisterRequest("new@test.com", "takenuser", "John", null, null, "Password1");
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UsernameAlreadyExistsException.class);
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
        when(companyRepository.save(any())).thenReturn(company);
        when(workerRepository.save(any())).thenReturn(worker);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("company-token");

        CompanyRegisterResponse result = authService.registerCompany(req);
        assertThat(result.token()).isEqualTo("company-token");
    }

    @Test
    void registerCompany_emailTaken_throws() {
        CompanyRegisterRequest req = new CompanyRegisterRequest(
                "taken@test.com", "Password1", "Org", "slug", "Company", "TRANSPORT", null, null, null, null);
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authService.registerCompany(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void registerCompany_handleTaken_throws() {
        CompanyRegisterRequest req = new CompanyRegisterRequest(
                "new@test.com", "Password1", "Org", "taken-slug", "Company", "TRANSPORT", null, null, null, null);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(organizationRepository.existsByHandle("taken-slug")).thenReturn(true);
        assertThatThrownBy(() -> authService.registerCompany(req))
                .isInstanceOf(HandleConflictException.class);
    }

    @Test
    void registerCompany_usernameTaken_throws() {
        CompanyRegisterRequest req = new CompanyRegisterRequest(
                "new@test.com", "Password1", "Org", "slug", "Company", "TRANSPORT", "takenuser", null, null, null);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(organizationRepository.existsByHandle("slug")).thenReturn(false);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);
        assertThatThrownBy(() -> authService.registerCompany(req))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    // --- availability checks ---

    @Test
    void isHandleAvailable_available_returnsTrue() {
        when(organizationRepository.existsByHandle("free-slug")).thenReturn(false);
        assertThat(authService.isHandleAvailable("free-slug")).isTrue();
    }

    @Test
    void isHandleAvailable_taken_returnsFalse() {
        when(organizationRepository.existsByHandle("taken-slug")).thenReturn(true);
        assertThat(authService.isHandleAvailable("taken-slug")).isFalse();
    }

    @Test
    void isUsernameAvailable_available_returnsTrue() {
        when(userRepository.existsByUsername("freeuser")).thenReturn(false);
        assertThat(authService.isUsernameAvailable("freeuser")).isTrue();
    }

    @Test
    void isUsernameAvailable_taken_returnsFalse() {
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);
        assertThat(authService.isUsernameAvailable("takenuser")).isFalse();
    }

}
