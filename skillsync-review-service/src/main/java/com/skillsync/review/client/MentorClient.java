package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @PutMapping("/mentors/{id}/rating")
    void updateMentorRating(
            @PathVariable("id") Long id,
            @RequestParam("rating") Double rating,
            @RequestHeader("X-Internal-Secret") String secret);
}