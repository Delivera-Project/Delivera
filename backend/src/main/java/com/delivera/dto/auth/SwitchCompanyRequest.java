package com.delivera.dto.auth;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SwitchCompanyRequest(@NotNull UUID companyId) {}
