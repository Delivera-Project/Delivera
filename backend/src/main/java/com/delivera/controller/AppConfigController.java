package com.delivera.controller;

import com.delivera.dto.config.AppConfigResponse;
import com.delivera.service.AppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app-config")
@Tag(name = "App Config", description = "Configuración dinámica de la aplicación")
public class AppConfigController {

    private final AppConfigService appConfigService;

    @Operation(summary = "Obtener configuración de la aplicación")
    @GetMapping
    public ResponseEntity<AppConfigResponse> getConfig() {
        return ResponseEntity.ok(appConfigService.getConfig());
    }
}
