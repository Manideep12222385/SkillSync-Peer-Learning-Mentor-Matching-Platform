package com.skillsync.mentor.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.skillsync.mentor.dto.SkillDto;

@FeignClient(name = "skill-service")
public interface SkillClient {

    @GetMapping("/skills/{skillId}")
    SkillDto getSkillById(@PathVariable Long skillId);
}