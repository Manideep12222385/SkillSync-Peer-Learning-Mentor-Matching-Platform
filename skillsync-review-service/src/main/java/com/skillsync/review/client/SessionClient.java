package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.skillsync.review.dto.SessionDTO;

@FeignClient(name = "session-service")
public interface SessionClient {

    @GetMapping("/sessions/{id}")
    SessionDTO getSession(@PathVariable Long id);
}