package com.delivera.service;

import com.delivera.dto.user.ChangePasswordRequest;
import com.delivera.dto.user.ProfileResponse;
import com.delivera.dto.user.UpdateProfileRequest;
import com.delivera.exception.InvalidPasswordException;
import com.delivera.exception.UserNotFoundException;
import com.delivera.model.User;
import com.delivera.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        return modelMapper.map(user, ProfileResponse.class);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        userRepository.save(user);

        return modelMapper.map(user, ProfileResponse.class);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("CURRENT_PASSWORD_INVALID");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("NEW_PASSWORD_SAME_AS_CURRENT");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

}
