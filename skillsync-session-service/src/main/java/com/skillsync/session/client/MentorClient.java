package com.skillsync.session.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/mentors/exists/{mentorId}")
    Boolean mentorExists(@PathVariable Long mentorId);
}