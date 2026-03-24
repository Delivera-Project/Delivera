package com.delivera.service;

import com.delivera.dto.user.ChangePasswordRequest;
import com.delivera.dto.user.ProfileResponse;
import com.delivera.dto.user.UpdateProfileRequest;
import com.delivera.exception.InvalidPasswordException;
import com.delivera.exception.UsernameAlreadyExistsException;
import com.delivera.exception.UserNotFoundException;
import com.delivera.model.User;
import com.delivera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return ProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (request.username() != null && !request.username().isBlank()) {
            if (!request.username().equals(user.getUsername()) && userRepository.existsByUsername(request.username())) {
                throw new UsernameAlreadyExistsException();
            }
            user.setUsername(request.username());
        }
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(StringUtils.hasText(request.phone()) ? request.phone() : null);
        userRepository.save(user);

        return ProfileResponse.from(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("CURRENT_PASSWORD_INVALID");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("NEW_PASSWORD_SAME_AS_CURRENT");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

}
