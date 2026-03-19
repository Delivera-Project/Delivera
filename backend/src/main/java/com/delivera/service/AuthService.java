package com.delivera.service;

import com.delivera.dto.auth.CompanyRegisterRequest;
import com.delivera.dto.auth.CompanyRegisterResponse;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.exception.EmailAlreadyExistsException;
import com.delivera.exception.InvalidCredentialsException;
import com.delivera.exception.SlugConflictException;
import com.delivera.model.*;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OrganizationRepository;
import com.delivera.repository.UserRepository;
import com.delivera.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> new LoginResponse(jwtService.generateToken(user.getEmail()), user.getEmail()))
                .orElseThrow(InvalidCredentialsException::new);
    }

    @Transactional
    public RegisterResponse register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new RegisterResponse(token, user.getEmail());
    }

    @Transactional
    public CompanyRegisterResponse registerCompany(CompanyRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        Organization organization = createOrganizationWithSlug(request.companyName());

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

        String token = jwtService.generateToken(user.getEmail());
        return new CompanyRegisterResponse(token, user.getEmail(), company.getId(), organization.getSlug());
    }

    private Organization createOrganizationWithSlug(String name) {
        String slug = generateUniqueSlug(name);
        DataIntegrityViolationException lastConflict = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Organization org = new Organization();
                org.setName(name);
                org.setSlug(slug);
                return organizationRepository.saveAndFlush(org);
            } catch (DataIntegrityViolationException e) {
                lastConflict = e;
                log.warn("Slug conflict for '{}' on attempt {}/3, retrying", slug, attempt);
                slug = generateUniqueSlug(name);
            }
        }
        throw new SlugConflictException(slug, lastConflict);
    }

    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-)|(-$)", "");
        if (!organizationRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 2;
        while (organizationRepository.existsBySlug(base + "-" + suffix)) {
            suffix++;
        }
        return base + "-" + suffix;
    }
}
