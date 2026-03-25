package com.skillsync.review.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reviewAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillSync Review Service")
                        .version("1.0")
                        .description("Mentor rating & review APIs"));
    }
}