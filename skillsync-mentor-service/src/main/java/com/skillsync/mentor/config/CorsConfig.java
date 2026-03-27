package com.skillsync.mentor.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow the Gateway's URL (localhost:8085)
        config.setAllowedOrigins(List.of("http://localhost:8085"));
        
        // Allow all standard headers and methods
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Essential if you're passing JWTs or Cookies
        config.setAllowCredentials(true);
        
        // Apply this to all paths in the microservice
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}