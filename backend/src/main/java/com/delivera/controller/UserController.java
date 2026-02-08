package com.delivera.controller;

import com.delivera.dto.ChangePasswordRequest;
import com.delivera.dto.ProfileResponse;
import com.delivera.dto.UpdateProfileRequest;
import com.delivera.repository.UserRepository;
import com.delivera.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          JwtService jwtService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Token no proporcionado"));
        }

        try {
            String email = jwtService.parseToken(authHeader.substring(7));

            return userRepository.findByEmail(email)
                    .map(user -> ResponseEntity.ok(new ProfileResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getPhone(),
                            user.getCreatedAt()
                    )))
                    .orElse(ResponseEntity.status(404).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido o expirado"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Token no proporcionado"));
        }

        try {
            String email = jwtService.parseToken(authHeader.substring(7));

            return userRepository.findByEmail(email)
                    .map(user -> {
                        user.setFirstName(request.firstName());
                        user.setLastName(request.lastName());
                        user.setPhone(request.phone());
                        userRepository.save(user);

                        return ResponseEntity.ok(new ProfileResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getPhone(),
                                user.getCreatedAt()
                        ));
                    })
                    .orElse(ResponseEntity.status(404).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido o expirado"));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Token no proporcionado"));
        }

        try {
            String email = jwtService.parseToken(authHeader.substring(7));

            return userRepository.findByEmail(email)
                    .map(user -> {
                        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                            return ResponseEntity.badRequest()
                                    .body((Object) Map.of("message", "La contraseña actual es incorrecta"));
                        }

                        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
                        userRepository.save(user);

                        return ResponseEntity.ok((Object) Map.of("message", "Contraseña actualizada correctamente"));
                    })
                    .orElse(ResponseEntity.status(404).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido o expirado"));
        }
    }
}
