package com.delivera.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderMessageRequest(
        @NotBlank @Size(max = 2000) String content) {
}
