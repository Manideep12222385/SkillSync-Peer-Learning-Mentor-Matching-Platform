package com.skillsync.skill.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.List;

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

        private static final String SECRET = "skillsyncsecretkeyskillsyncsecretkey";

        // ⭐ inject custom handlers
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthEntryPoint customAuthEntryPoint;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET, "/skills", "/skills/search", "/skills/exists/**").permitAll()
                                                .requestMatchers("/skills/**").hasRole("ADMIN")
                                                .anyRequest().permitAll())

                                // ⭐ ADD THIS BLOCK (very important)
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(customAccessDeniedHandler)
                                                .authenticationEntryPoint(customAuthEntryPoint))

                                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter())));

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {

                SecretKey key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");

                return NimbusJwtDecoder
                                .withSecretKey(key)
                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

                converter.setAuthoritiesClaimName("role");
                converter.setAuthorityPrefix("");
                JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();

                authConverter.setJwtGrantedAuthoritiesConverter(converter);

                return authConverter;
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