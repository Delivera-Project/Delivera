package com.delivera.config;

import com.delivera.model.ApiKey;
import com.delivera.repository.ApiKeyRepository;
import com.delivera.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";
    private static final String ROLE = "ROLE_API_KEY";

    private final ApiKeyRepository apiKeyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header == null || header.isBlank() || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.trim();
        Optional<ApiKey> found = apiKeyRepository.findByKeyHash(ApiKeyService.hash(token));
        if (found.isEmpty() || found.get().getRevokedAt() != null) {
            sendError(response);
            return;
        }

        ApiKey key = found.get();
        key.setLastUsedAt(Instant.now());
        apiKeyRepository.save(key);

        var authorities = List.of(new SimpleGrantedAuthority(ROLE));
        var authentication = new UsernamePasswordAuthenticationToken(
                "api-key:" + key.getId(), null, authorities);
        authentication.setDetails(key.getCompany().getId());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("code", "API_KEY_INVALID"));
    }
}
