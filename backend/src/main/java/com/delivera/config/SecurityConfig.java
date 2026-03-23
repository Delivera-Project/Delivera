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

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
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
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/organizations/**").permitAll()
                        .requestMatchers("/api/v1/activity-types/**").permitAll()
                        .requestMatchers("/api/v1/app-config/**").permitAll()
                        .requestMatchers("/api/v1/orders/public/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("GLOBAL_ADMIN")
                        .requestMatchers("/api/v1/settings/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/units/external").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST")
                        .requestMatchers(HttpMethod.POST, "/api/v1/units").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/units/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/units/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasAnyRole(ROLE_COMPANY_ADMIN, "ANALYST", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole(ROLE_COMPANY_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/loyal-users").hasRole(ROLE_COMPANY_ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
