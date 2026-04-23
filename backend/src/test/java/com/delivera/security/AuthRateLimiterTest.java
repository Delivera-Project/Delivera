package com.delivera.security;

import com.delivera.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRateLimiterTest {

    private final AuthRateLimiter limiter = new AuthRateLimiter(5, 60_000L);

    @Test
    void check_underLimit_allows_then_throws_when_exceeded() {
        for (int i = 0; i < 5; i++) {
            assertThatCode(() -> limiter.check("1.1.1.1", "login")).doesNotThrowAnyException();
        }
        assertThatThrownBy(() -> limiter.check("1.1.1.1", "login"))
                .isInstanceOf(RateLimitExceededException.class);
        assertThatCode(() -> limiter.check("2.2.2.2", "login")).doesNotThrowAnyException();
    }
}
