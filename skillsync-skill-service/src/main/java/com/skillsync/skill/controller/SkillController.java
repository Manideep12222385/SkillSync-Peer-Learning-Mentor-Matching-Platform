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

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: SkillController
 * DESCRIPTION:
 * This controller handles skill-related operations including creating,
 * updating, deleting, and searching active skills.
 * ================================================================
 */
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /* ================================================================
     * METHOD: createSkill
     * DESCRIPTION:
     * Allows admins to create a new skill in the system.
     * ================================================================ */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public SkillResponseDto createSkill(
            @Valid @RequestBody CreateSkillRequestDto request) {

        return skillService.createSkill(request);
    }

    /* ================================================================
     * METHOD: updateSkill
     * DESCRIPTION:
     * Allows admins to update an existing skill's details.
     * ================================================================ */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{skillId}")
    public SkillResponseDto updateSkill(
            @PathVariable Long skillId,
            @RequestBody UpdateSkillRequestDto request) {

        return skillService.updateSkill(skillId, request);
    }

    /* ================================================================
     * METHOD: deleteSkill
     * DESCRIPTION:
     * Allows admins to soft delete a skill from the system.
     * ================================================================ */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{skillId}")
    public String deleteSkill(@PathVariable Long skillId) {

        return skillService.deleteSkill(skillId);
    }

    // ⭐ PUBLIC — Get Active Skills (Marketplace dropdown)
    /* ================================================================
     * METHOD: getAllActiveSkills
     * DESCRIPTION:
     * Retrieves a paginated list of all active skills.
     * ================================================================ */
    @GetMapping
    public Page<Skill> getAllActiveSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return skillService.getAllActiveSkills(page, size);
    }

    // ⭐ PUBLIC — Search Skills
    /* ================================================================
     * METHOD: searchSkills
     * DESCRIPTION:
     * Searches for active skills matching the provided keyword.
     * ================================================================ */
    @GetMapping("/search")
    public Page<Skill> searchSkills(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return skillService.searchSkills(keyword, page, size);
    }

    // ⭐ FEIGN VALIDATION — Skill Exists
    /* ================================================================
     * METHOD: skillExists
     * DESCRIPTION:
     * Validates if a skill exists by its ID. Used internally via Feign.
     * ================================================================ */
    @GetMapping("/exists/{skillId}")
    public Boolean skillExists(@PathVariable Long skillId) {

        return skillService.skillExists(skillId);
    }
}