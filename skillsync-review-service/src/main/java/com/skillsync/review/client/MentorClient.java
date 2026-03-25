package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @PutMapping("/mentors/{id}/rating")
    void updateMentorRating(
            @PathVariable Long id,
            @RequestParam Double rating);
}