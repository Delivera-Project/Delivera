package com.delivera.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String lastName,

        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        String phone
) {}
