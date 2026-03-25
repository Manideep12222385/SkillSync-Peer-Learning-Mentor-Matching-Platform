package com.skillsync.skill.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String SECRET =
            "skillsyncsecretkeyskillsyncsecretkey";

    // ⭐ inject custom handlers
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/skills/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            )

            // ⭐ ADD THIS BLOCK (very important)
            .exceptionHandling(ex -> ex
                    .accessDeniedHandler(customAccessDeniedHandler)
                    .authenticationEntryPoint(customAuthEntryPoint)
            )

            .oauth2ResourceServer(oauth ->
                    oauth.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(
                                    jwtAuthenticationConverter()
                            )
                    )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey key =
                new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");

        return NimbusJwtDecoder
                .withSecretKey(key)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter converter =
                new JwtGrantedAuthoritiesConverter();

        converter.setAuthoritiesClaimName("role");
        converter.setAuthorityPrefix("");

        JwtAuthenticationConverter authConverter =
                new JwtAuthenticationConverter();

        authConverter.setJwtGrantedAuthoritiesConverter(converter);

        return authConverter;
    }
}