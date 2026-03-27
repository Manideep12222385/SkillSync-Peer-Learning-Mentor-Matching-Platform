//package com.skillsync.auth.config;
//
//import java.util.Arrays;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.
//        configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.
//        UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import com.skillsync.auth.security.JwtAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//
//    @Autowired
//    private JwtAuthenticationFilter jwtFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http)
//            throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .authorizeHttpRequests(auth -> auth
//
//                        // ⭐ PUBLIC AUTH APIs
//                        .requestMatchers(
//                                "/auth/register",
//                                "/auth/login",
//                                "/auth/reset-password",
//                                "/auth/forgot-password",
//                                "/auth/internal/**",
//
//                                // ⭐ SWAGGER URLs VERY IMPORTANT
//                                "/v3/api-docs",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/actuator/**"
//                        ).permitAll()
//
//                        // ⭐ ADMIN APIs
//                        .requestMatchers("/admin/**")
//                        .hasRole("ADMIN")
//
//                        // ⭐ MENTOR APIs
//                        .requestMatchers("/mentor/**")
//                        .hasAnyRole("MENTOR", "ADMIN")
//
//                        // ⭐ LEARNER APIs
//                        .requestMatchers("/learner/**")
//                        .hasAnyRole("LEARNER", "MENTOR", "ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(
//                                SessionCreationPolicy.STATELESS))
//
//                .addFilterBefore(
//                        jwtFilter,
//                        UsernamePasswordAuthenticationFilter.class
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config)
//            throws Exception {
//        return config.getAuthenticationManager();
//    }
//    
//    
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        
//        // Allow the specific origin of your Gateway/Swagger UI
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8085")); 
//        
//        // Allow standard methods
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        
//        // Allow all headers (Content-Type, Authorization, etc.)
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        
//        // Allow credentials if your fetch needs to send cookies or Auth headers
//        configuration.setAllowCredentials(true);
//        
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

package com.skillsync.auth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.skillsync.auth.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Reference the CorsConfigurationSource bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        // ⭐ PUBLIC AUTH APIs
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/reset-password",
                                "/auth/forgot-password",
                                "/auth/internal/**",

                                // ⭐ SWAGGER URLs
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()

                        // ⭐ ADMIN APIs
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // ⭐ MENTOR APIs
                        .requestMatchers("/mentor/**")
                        .hasAnyRole("MENTOR", "ADMIN")

                        // ⭐ LEARNER APIs
                        .requestMatchers("/learner/**")
                        .hasAnyRole("LEARNER", "MENTOR", "ADMIN")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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