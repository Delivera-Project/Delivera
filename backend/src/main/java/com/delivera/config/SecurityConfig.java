package com.delivera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    private static final String ADMIN    = "COMPANY_ADMIN";
    private static final String ANALYST  = "ANALYST";
    private static final String OPERATOR = "OPERATOR";
    private static final String UNITS_ALL = "/units/**";

    // Paths de documentación y endpoints públicos sin prefijo de API
    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**"
    };

    @Value("${app.api-prefix}")
    private String api;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // Documentación y acceso público
                auth.requestMatchers(SWAGGER_PATHS).permitAll();
                auth.requestMatchers(api + "/auth/**").permitAll();
                auth.requestMatchers(HttpMethod.GET, api + "/organizations/**").permitAll();
                auth.requestMatchers(HttpMethod.GET, api + "/activity-types/**").permitAll();
                auth.requestMatchers(HttpMethod.GET, api + "/app-config/**").permitAll();
                auth.requestMatchers(api + "/orders/public/**").permitAll();

                // Administración global
                auth.requestMatchers(api + "/admin/**").hasRole("GLOBAL_ADMIN");

                // Configuración de empresa — solo admins
                auth.requestMatchers(api + "/settings/**").hasRole(ADMIN);

                // Unidades operativas
                auth.requestMatchers(HttpMethod.GET, api + "/units/external").hasAnyRole(ADMIN, ANALYST);
                auth.requestMatchers(HttpMethod.GET, api + "/units/external-companies").hasAnyRole(ADMIN, ANALYST);
                auth.requestMatchers(HttpMethod.POST, api + "/units").hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.POST, api + "/units/*/workers/*").hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.PUT, api + UNITS_ALL).hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.DELETE, api + "/units/*/workers/*").hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.DELETE, api + UNITS_ALL).hasRole(ADMIN);

                // Pedidos
                auth.requestMatchers(HttpMethod.POST, api + "/orders").hasAnyRole(ADMIN, ANALYST);
                auth.requestMatchers(HttpMethod.PATCH, api + "/orders/*/status").hasAnyRole(ADMIN, ANALYST, OPERATOR);
                auth.requestMatchers(HttpMethod.DELETE, api + "/orders/**").hasRole(ADMIN);

                // Usuarios fidelizados
                auth.requestMatchers(HttpMethod.POST, api + "/loyal-users").hasRole(ADMIN);

                // Trabajadores
                auth.requestMatchers(HttpMethod.POST, api + "/workers/invite").hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.PATCH, api + "/workers/*/role").hasRole(ADMIN);
                auth.requestMatchers(HttpMethod.DELETE, api + "/workers/**").hasRole(ADMIN);

                // Endpoints autenticados: cualquier trabajador activo
                auth.requestMatchers(api + "/workers/**").authenticated();
                auth.requestMatchers(api + UNITS_ALL).authenticated();
                auth.requestMatchers(api + "/orders/**").authenticated();
                auth.requestMatchers(api + "/loyal-users/**").authenticated();
                auth.requestMatchers(api + "/user/**").authenticated();

                auth.anyRequest().denyAll();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
        );
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
