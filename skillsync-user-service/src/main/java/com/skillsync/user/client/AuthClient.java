package com.skillsync.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/validate/{userId}")
    Boolean validateUser(@PathVariable Long userId);
}