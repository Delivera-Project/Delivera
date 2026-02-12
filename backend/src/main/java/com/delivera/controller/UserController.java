package com.delivera.controller;

import com.delivera.dto.MessageResponse;
import com.delivera.dto.user.ChangePasswordRequest;
import com.delivera.dto.user.ProfileResponse;
import com.delivera.dto.user.UpdateProfileRequest;
import com.delivera.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        ProfileResponse profile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse profile = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new MessageResponse("PASSWORD_CHANGED"));
    }
}
