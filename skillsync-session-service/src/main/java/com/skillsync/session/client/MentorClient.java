package com.skillsync.session.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    // ⭐ fetch mentor profile id using auth userId
    @GetMapping("/mentors/by-user/{userId}")
    Long getMentorProfileId(@PathVariable("userId") Long userId);
}