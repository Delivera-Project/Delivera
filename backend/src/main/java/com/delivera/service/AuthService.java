package com.delivera.service;

import com.delivera.dto.auth.ClaimRegisterRequest;
import com.delivera.dto.auth.CompanyRegisterRequest;
import com.delivera.dto.auth.CompanyRegisterResponse;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterRequest;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.exception.*;
import com.delivera.model.*;
import com.delivera.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private WorkerRepository workerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private LoyalUserRepository loyalUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    public LoginResponse login(String identifier, String password) {
        User user = userRepository.findByEmailOrUsername(identifier)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        List<Worker> workers = workerRepository.findByUserEmailOrderByCreatedAtAsc(user.getEmail());
        if (!workers.isEmpty()) {
            Worker worker = workers.get(0);
            Organization org = worker.getCompany().getOrganization();
            String token = jwtService.generateToken(user.getEmail(), worker.getCompany().getId(), worker.getRole());
            return new LoginResponse(token, user.getEmail(), worker.getCompany().getId(),
                    worker.getRole().name(), worker.getCompany().getName(), org.getHandle(), org.getName());
        }

        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(token, user.getEmail(), null, null, null, null, null);
    }

    public LoginResponse switchCompany(String email, UUID targetCompanyId) {
        Worker worker = workerRepository.findByUserEmailAndCompanyId(email, targetCompanyId)
                .orElseThrow(InvalidCredentialsException::new);
        Organization org = worker.getCompany().getOrganization();
        String token = jwtService.generateToken(email, targetCompanyId, worker.getRole());
        return new LoginResponse(token, email, targetCompanyId, worker.getRole().name(),
                worker.getCompany().getName(), org.getHandle(), org.getName());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }
        User user = buildUser(request.email(), request.username(), request.firstName(), request.lastName(), request.phone(), request.password());
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return new RegisterResponse(token, user.getEmail());
    }

    public boolean isHandleAvailable(String handle) {
        return !organizationRepository.existsByHandle(handle);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional
    public CompanyRegisterResponse registerCompany(CompanyRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        if (organizationRepository.existsByHandle(request.orgHandle())) {
            throw new HandleConflictException(request.orgHandle(), null);
        }

        if (request.username() != null && !request.username().isBlank()
                && userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = buildUser(request.email(), request.username(), request.firstName(), request.lastName(), request.phone(), request.password());
        userRepository.save(user);

        Organization organization = new Organization();
        organization.setName(request.orgName());
        organization.setHandle(request.orgHandle());
        organizationRepository.saveAndFlush(organization);

        Company company = new Company();
        company.setOrganization(organization);
        company.setName(request.companyName());
        company.setActivityType(request.activityType());
        companyRepository.save(company);

        Worker worker = new Worker();
        worker.setUser(user);
        worker.setCompany(company);
        worker.setRole(WorkerRole.COMPANY_ADMIN);
        workerRepository.save(worker);

        String token = jwtService.generateToken(user.getEmail(), company.getId(), WorkerRole.COMPANY_ADMIN);
        return new CompanyRegisterResponse(token, user.getEmail(), company.getId(),
                WorkerRole.COMPANY_ADMIN.name(), company.getName(), organization.getHandle(), organization.getName());
    }

    @Transactional
    public LoginResponse claimRegister(String token, ClaimRegisterRequest request) {
        var order = orderRepository.findByTrackingToken(token)
                .orElseThrow(OrderNotFoundException::new);

        if (order.getLoyalUser() != null && order.getLoyalUser().getUser() != null) {
            throw new OrderAlreadyClaimedException();
        }

        String email = request.email().toLowerCase().trim();
        if (!email.equalsIgnoreCase(order.getRecipientEmail())) {
            throw new OrderClaimEmailMismatchException();
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = buildUser(email, null, request.firstName(), request.lastName(), null, request.password());
        userRepository.save(user);

        LoyalUser loyalUser = loyalUserRepository
                .findByCompanyIdAndEmail(order.getCompany().getId(), email)
                .orElseGet(() -> {
                    var lu = new LoyalUser();
                    lu.setCompany(order.getCompany());
                    lu.setEmail(email);
                    return lu;
                });
        loyalUser.setUser(user);
        loyalUserRepository.save(loyalUser);

        order.setLoyalUser(loyalUser);
        orderRepository.save(order);

        String jwtToken = jwtService.generateToken(user.getEmail());
        return new LoginResponse(jwtToken, user.getEmail(), null, "LOYAL_USER", null, null, null);
    }

    private User buildUser(String email, String username, String firstName, String lastName, String phone, String password) {
        User user = new User();
        user.setEmail(email);
        if (StringUtils.hasText(username)) user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(StringUtils.hasText(phone) ? phone : null);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}
