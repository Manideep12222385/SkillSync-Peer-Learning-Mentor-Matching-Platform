package com.skillsync.session.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/exists/{userId}")
    Boolean userExists(@PathVariable("userId") Long userId);
}