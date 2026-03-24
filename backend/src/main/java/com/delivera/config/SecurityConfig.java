package com.delivera.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROLE_COMPANY_ADMIN = "COMPANY_ADMIN";

    @Value("${app.api-prefix}")
    private String apiPrefix;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers(apiPrefix + "/auth/**").permitAll()
                        .requestMatchers(apiPrefix + "/organizations/**").permitAll()
                        .requestMatchers(apiPrefix + "/activity-types/**").permitAll()
                        .requestMatchers(apiPrefix + "/app-config/**").permitAll()
                        .requestMatchers(apiPrefix + "/orders/public/**").permitAll()
                        .requestMatchers(apiPrefix + "/admin/**").hasRole("GLOBAL_ADMIN")
                        .requestMatchers(apiPrefix + "/settings/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.GET, apiPrefix + "/units/external").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST")
                        .requestMatchers(HttpMethod.POST, apiPrefix + "/units").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.PUT, apiPrefix + "/units/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, apiPrefix + "/units/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.POST, apiPrefix + "/orders").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST")
                        .requestMatchers(HttpMethod.PATCH, apiPrefix + "/orders/*/status").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, apiPrefix + "/orders/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.POST, apiPrefix + "/loyal-users").hasRole(ROLE_COMPANY_ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
        );
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
