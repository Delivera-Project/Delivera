package com.delivera.config;

import com.delivera.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthHeader_proceedsWithoutAuth() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validWorkerToken_setsAuthenticationWithCompany() throws Exception {
        UUID companyId = UUID.randomUUID();
        JwtService.TokenClaims claims = new JwtService.TokenClaims("admin@test.com", "COMPANY_ADMIN", companyId);
        when(jwtService.parseTokenWithClaims("valid-token")).thenReturn(claims);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("admin@test.com");
        assertThat(auth.getDetails()).isEqualTo(companyId);
    }

    @Test
    void validIndividualToken_setsAuthenticationWithoutCompany() throws Exception {
        JwtService.TokenClaims claims = new JwtService.TokenClaims("user@test.com", null, null);
        when(jwtService.parseTokenWithClaims("ind-token")).thenReturn(claims);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer ind-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    void expiredToken_returns401TokenExpired() throws Exception {
        when(jwtService.parseTokenWithClaims(any()))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(res.getContentAsString()).contains("TOKEN_EXPIRED");
    }

    @Test
    void invalidToken_returns401TokenInvalid() throws Exception {
        when(jwtService.parseTokenWithClaims(any()))
                .thenThrow(new JwtException("invalid"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(res.getContentAsString()).contains("TOKEN_INVALID");
    }

    @Test
    void unexpectedExceptionDuringParsing_returns401TokenInvalid() throws Exception {
        when(jwtService.parseTokenWithClaims(any()))
                .thenThrow(new RuntimeException("unexpected"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(res.getContentAsString()).contains("TOKEN_INVALID");
    }
}
