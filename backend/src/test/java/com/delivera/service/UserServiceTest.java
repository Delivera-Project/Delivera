package com.delivera.service;

import com.delivera.dto.user.ChangePasswordRequest;
import com.delivera.dto.user.ProfileResponse;
import com.delivera.dto.user.UpdateProfileRequest;
import com.delivera.exception.InvalidPasswordException;
import com.delivera.exception.UserNotFoundException;
import com.delivera.exception.UsernameAlreadyExistsException;
import com.delivera.model.User;
import com.delivera.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashed");
    }

    @Test
    void getProfile_found_returnsProfileResponse() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        ProfileResponse result = userService.getProfile("user@test.com");
        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    void getProfile_notFound_throws() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getProfile("missing@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_success() {
        UpdateProfileRequest req = new UpdateProfileRequest("newusername", "John", "Doe", null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile("user@test.com", req);
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_sameUsername_doesNotCheckConflict() {
        UpdateProfileRequest req = new UpdateProfileRequest("testuser", "John", "Doe", null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile("user@test.com", req);
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    void updateProfile_usernameTaken_throws() {
        UpdateProfileRequest req = new UpdateProfileRequest("taken", "John", "Doe", null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        assertThatThrownBy(() -> userService.updateProfile("user@test.com", req))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void updateProfile_notFound_throws() {
        UpdateProfileRequest req = new UpdateProfileRequest("u", "John", null, null);
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateProfile("missing@test.com", req))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest req = new ChangePasswordRequest("oldPass", "NewPass1");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("NewPass1", "hashed")).thenReturn(false);
        when(passwordEncoder.encode("NewPass1")).thenReturn("newHashed");

        userService.changePassword("user@test.com", req);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_currentPasswordWrong_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest("wrong", "NewPass1");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        assertThatThrownBy(() -> userService.changePassword("user@test.com", req))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("CURRENT_PASSWORD_INVALID");
    }

    @Test
    void changePassword_sameAsCurrentPassword_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest("oldPass", "oldPass");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashed")).thenReturn(true);
        assertThatThrownBy(() -> userService.changePassword("user@test.com", req))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("NEW_PASSWORD_SAME_AS_CURRENT");
    }

    @Test
    void changePassword_userNotFound_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest("pass", "NewPass1");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.changePassword("missing@test.com", req))
                .isInstanceOf(UserNotFoundException.class);
    }
}
