package com.skillsync.session.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sessionAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillSync Session Service")
                        .version("1.0")
                        .description("Mentor session booking APIs"));
    }
}