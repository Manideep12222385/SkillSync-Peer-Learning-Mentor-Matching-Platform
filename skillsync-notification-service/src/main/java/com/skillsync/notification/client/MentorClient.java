package com.skillsync.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/mentors/internal/{mentorId}/userid")
    Long getUserIdByMentorId(@PathVariable("mentorId") Long mentorId);
}
