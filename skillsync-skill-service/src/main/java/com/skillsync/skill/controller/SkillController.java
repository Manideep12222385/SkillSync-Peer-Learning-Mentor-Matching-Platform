package com.skillsync.skill.controller;

import com.skillsync.skill.dto.CreateSkillRequestDto;
import com.skillsync.skill.dto.SkillResponseDto;
import com.skillsync.skill.dto.UpdateSkillRequestDto;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // ⭐ ADMIN ONLY — Create Skill
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public SkillResponseDto createSkill(
            @Valid @RequestBody CreateSkillRequestDto request) {

        return skillService.createSkill(request);
    }

    // ⭐ ADMIN ONLY — Update Skill
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{skillId}")
    public SkillResponseDto updateSkill(
            @PathVariable Long skillId,
            @RequestBody UpdateSkillRequestDto request) {

        return skillService.updateSkill(skillId, request);
    }

    // ⭐ ADMIN ONLY — Soft Delete Skill
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{skillId}")
    public String deleteSkill(@PathVariable Long skillId) {

        return skillService.deleteSkill(skillId);
    }

    // ⭐ PUBLIC — Get Active Skills (Marketplace dropdown)
    @GetMapping
    public Page<Skill> getAllActiveSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return skillService.getAllActiveSkills(page, size);
    }

    // ⭐ PUBLIC — Search Skills
    @GetMapping("/search")
    public Page<Skill> searchSkills(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return skillService.searchSkills(keyword, page, size);
    }

    // ⭐ FEIGN VALIDATION — Skill Exists
    @GetMapping("/exists/{skillId}")
    public Boolean skillExists(@PathVariable Long skillId) {

        return skillService.skillExists(skillId);
    }
}