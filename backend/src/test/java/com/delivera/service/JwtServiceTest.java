package com.delivera.service;

import com.delivera.model.WorkerRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "delivera-jwt-secret-key-para-desarrollo-test-long-enough";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600L);
    }

    @Test
    void generateToken_emailOnly_returnsNonNull() {
        String token = jwtService.generateToken("user@test.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_emailOnly_parseable() {
        String token = jwtService.generateToken("user@test.com");
        JwtService.TokenClaims claims = jwtService.parseTokenWithClaims(token);
        assertThat(claims.email()).isEqualTo("user@test.com");
        assertThat(claims.role()).isNull();
        assertThat(claims.companyId()).isNull();
    }

    @Test
    void generateToken_withCompanyAndRole_parseable() {
        UUID companyId = UUID.randomUUID();
        String token = jwtService.generateToken("worker@test.com", companyId, WorkerRole.COMPANY_ADMIN);
        JwtService.TokenClaims claims = jwtService.parseTokenWithClaims(token);
        assertThat(claims.email()).isEqualTo("worker@test.com");
        assertThat(claims.role()).isEqualTo("COMPANY_ADMIN");
        assertThat(claims.companyId()).isEqualTo(companyId);
    }

    @Test
    void generateToken_withRole_throwsOnNullCompanyId() {
        assertThatThrownBy(() -> jwtService.generateToken("a@b.com", null, WorkerRole.OPERATOR))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void generateToken_withRole_throwsOnNullRole() {
        assertThatThrownBy(() -> jwtService.generateToken("a@b.com", UUID.randomUUID(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseTokenWithClaims_invalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.parseTokenWithClaims("not.a.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseTokenWithClaims_expiredToken_throwsJwtException() {
        JwtService shortLived = new JwtService(SECRET, 0L);
        String token = shortLived.generateToken("exp@test.com");
        assertThatThrownBy(() -> jwtService.parseTokenWithClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseTokenWithClaims_tokenFromDifferentKey_throwsJwtException() {
        JwtService other = new JwtService("completely-different-secret-key-must-be-long-enough", 3600L);
        String token = other.generateToken("x@y.com");
        assertThatThrownBy(() -> jwtService.parseTokenWithClaims(token))
                .isInstanceOf(JwtException.class);
    }
}
