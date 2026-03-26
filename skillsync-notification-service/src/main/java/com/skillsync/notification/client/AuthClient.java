package com.skillsync.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", url = "http://localhost:8081")
public interface AuthClient {

    @GetMapping("/auth/internal/users/{userId}/email")
    String getUserEmail(@PathVariable("userId") Long userId);

    @GetMapping("/auth/internal/users/{userId}/name")
    String getUserName(@PathVariable("userId") Long userId);
}
