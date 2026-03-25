package com.skillsync.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillSync User Service")
                        .version("1.0")
                        .description("User profile management APIs"));
    }
}