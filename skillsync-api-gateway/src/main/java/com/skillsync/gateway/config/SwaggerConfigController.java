package com.skillsync.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class SwaggerConfigController {

    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> swaggerConfig() {

        Map<String, Object> config = new HashMap<>();

        List<Map<String, String>> urls = new ArrayList<>();

        urls.add(create("Auth Service", "/auth-service/v3/api-docs"));
        urls.add(create("User Service", "/user-service/v3/api-docs"));
        urls.add(create("Mentor Service", "/mentor-service/v3/api-docs"));
        urls.add(create("Session Service", "/session-service/v3/api-docs"));
        urls.add(create("Review Service", "/review-service/v3/api-docs"));
        urls.add(create("Skill Service", "/skill-service/v3/api-docs"));

        config.put("urls", urls);

        return config;
    }

    private Map<String, String> create(String name, String url) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("url", url);
        return map;
    }
}