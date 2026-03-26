package com.skillsync.session.config;

import com.skillsync.session.security.JwtAuthConverter;
import com.skillsync.session.security.JwtAuthEntryPoint;
import com.skillsync.session.security.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                ).permitAll()

                // ⭐ mentor creates slot
                .requestMatchers(HttpMethod.POST, "/sessions/slots")
                .hasRole("MENTOR")

                // ⭐ learner books slot
                .requestMatchers(HttpMethod.PUT, "/sessions/*/request")
                .hasRole("LEARNER")

                // ⭐ mentor decisions
                .requestMatchers(HttpMethod.PUT,
                        "/sessions/*/accept",
                        "/sessions/*/reject",
                        "/sessions/*/complete")
                .hasRole("MENTOR")

                // ⭐ learner cancel
                .requestMatchers(HttpMethod.PUT, "/sessions/*/cancel")
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
}