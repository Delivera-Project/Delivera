package com.delivera.controller;

import com.delivera.dto.common.AvailabilityCheckResponse;
import com.delivera.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@Tag(name = "Organizaciones", description = "Endpoints públicos de organizaciones")
public class OrganizationController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Comprobar disponibilidad de handle de organización")
    @GetMapping("/check-handle")
    public ResponseEntity<AvailabilityCheckResponse> checkHandle(@RequestParam String handle) {
        return ResponseEntity.ok(new AvailabilityCheckResponse(authService.isHandleAvailable(handle)));
    }
}
