package com.skillsync.session.config;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    private final String SECRET = "skillsyncsecretkeyskillsyncsecretkey";

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKeySpec key =
                new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}