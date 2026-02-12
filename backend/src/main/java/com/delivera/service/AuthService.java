package com.delivera.service;

import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.exception.EmailAlreadyExistsException;
import com.delivera.exception.InvalidCredentialsException;
import com.delivera.model.User;
import com.delivera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

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
}
