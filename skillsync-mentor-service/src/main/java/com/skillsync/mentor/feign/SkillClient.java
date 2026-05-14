package com.skillsync.mentor.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;



@FeignClient(name = "skill-service")
public interface SkillClient {

    @GetMapping("/skills/exists/{skillId}")
    Boolean skillExists(@PathVariable("skillId") Long skillId);
}