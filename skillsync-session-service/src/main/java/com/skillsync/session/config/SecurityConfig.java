package com.skillsync.session.config;

import com.skillsync.session.security.JwtAuthConverter;
import com.skillsync.session.security.JwtAuthEntryPoint;
import com.skillsync.session.security.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                ).permitAll()

                // ⭐ mentor creates slot
                .requestMatchers(HttpMethod.POST, "/sessions/createSlot")
                .hasRole("MENTOR")

                // ⭐ learner books slot
                .requestMatchers(HttpMethod.POST, "/sessions/*/request")
                .hasRole("LEARNER")

                // ⭐ mentor decisions
                .requestMatchers(HttpMethod.POST,
                        "/sessions/*/accept",
                        "/sessions/*/reject",
                        "/sessions/*/complete")
                .hasRole("MENTOR")

                // ⭐ learner cancel
                .requestMatchers(HttpMethod.POST, "/sessions/*/cancel")
                .hasRole("LEARNER")

                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )

            .oauth2ResourceServer(oauth ->
                    oauth.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(new JwtAuthConverter())
                    )
            );

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow the specific origin of your Gateway/Swagger UI
        configuration.setAllowedOrigins(List.of("http://localhost:8085"));

        // Allow standard methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (Content-Type, Authorization, etc.)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials for Auth headers
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Applying this configuration to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}