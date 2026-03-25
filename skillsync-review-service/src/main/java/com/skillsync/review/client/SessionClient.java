package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "session-service")
public interface SessionClient {

    @GetMapping("/sessions/{id}/completed")
    Boolean isSessionCompleted(@PathVariable Long id);
}