package com.delivera.config;

import com.delivera.security.SecurityUtils;

import com.delivera.exception.CompanyContextException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentCompanyId_nullAuth_throws() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> securityUtils.getCurrentCompanyId())
                .isInstanceOf(CompanyContextException.class);
    }

    @Test
    void getCurrentEmail_withAuth_returnsEmail() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.getCurrentEmail()).isEqualTo("user@test.com");
    }

}
