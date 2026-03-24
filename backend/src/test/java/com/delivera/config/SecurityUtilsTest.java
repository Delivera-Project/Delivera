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
    void getCurrentCompanyId_withValidAuth_returnsCompanyId() {
        UUID companyId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());
        auth.setDetails(companyId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.getCurrentCompanyId()).isEqualTo(companyId);
    }

    @Test
    void getCurrentCompanyId_nullAuth_throws() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> securityUtils.getCurrentCompanyId())
                .isInstanceOf(CompanyContextException.class);
    }

    @Test
    void getCurrentCompanyId_detailsNotUUID_throws() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());
        auth.setDetails("not-a-uuid");
        SecurityContextHolder.getContext().setAuthentication(auth);

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

    @Test
    void getCurrentEmail_nullAuth_returnsNull() {
        SecurityContextHolder.clearContext();
        assertThat(securityUtils.getCurrentEmail()).isNull();
    }
}
