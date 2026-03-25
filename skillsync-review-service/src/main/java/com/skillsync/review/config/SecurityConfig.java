package com.skillsync.review.config;

import com.skillsync.review.security.JwtAuthEntryPoint;
import com.skillsync.review.security.JwtAuthConverter;
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

	private final JwtAuthEntryPoint entryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

						.requestMatchers(HttpMethod.GET, "/reviews/mentor/**").permitAll()

						// ⭐ ONLY LEARNER CAN REVIEW
						.requestMatchers(HttpMethod.POST, "/reviews").hasRole("LEARNER")

						.requestMatchers(HttpMethod.DELETE, "/reviews/**").hasRole("LEARNER")

						.anyRequest().authenticated())

				.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))

				.oauth2ResourceServer(
						oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthConverter())));

		return http.build();
	}
}