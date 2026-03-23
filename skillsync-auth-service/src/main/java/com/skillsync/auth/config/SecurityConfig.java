package com.skillsync.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.
        configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.
        UsernamePasswordAuthenticationFilter;

import com.skillsync.auth.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // public
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        // admin APIs
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // mentor APIs
                        .requestMatchers("/mentor/**")
                        .hasAnyRole("MENTOR","ADMIN")

                        // learner APIs
                        .requestMatchers("/learner/**")
                        .hasAnyRole("LEARNER","MENTOR","ADMIN")

                        // others secured
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}