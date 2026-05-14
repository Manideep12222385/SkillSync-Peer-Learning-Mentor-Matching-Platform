package com.skillsync.mentor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.skillsync.mentor.dto.*;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.security.JwtUtil;
import com.skillsync.mentor.service.MentorService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: MentorController
 * DESCRIPTION:
 * This controller handles mentor operations including profile creation,
 * updating skills, managing mentor search filters by price, rating and skills.
 * ================================================================
 */
@RestController
@RequestMapping("/mentors")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @Autowired
    private JwtUtil jwtUtil;

    /* ================================================================
     * METHOD: createProfile
     * DESCRIPTION:
     * Creates a mentor profile corresponding to an authenticated user ID.
     * ================================================================ */
    @PostMapping("/profile")
    public MentorProfileResponseDto createProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateMentorProfileRequestDto request) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.createProfile(userId, request);
    }

    /* ================================================================
     * METHOD: getProfile
     * DESCRIPTION:
     * Retrieves the authenticated mentor's profile.
     * ================================================================ */
    @GetMapping("/profile")
    public MentorProfileResponseDto getProfile(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);
        return mentorService.getProfile(userId);
    }

    /* ================================================================
     * METHOD: updateProfile
     * DESCRIPTION:
     * Updates an existing mentor profile with newer data such as bio,
     * experience, pricing, etc.
     * ================================================================ */
    @PutMapping("/profile")
    public MentorProfileResponseDto updateProfile(
            HttpServletRequest httpRequest,
            @RequestBody UpdateMentorProfileRequestDto request) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.updateProfile(userId, request);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    /* ================================================================
     * METHOD: addSkillPut
     * DESCRIPTION:
     * Adds an existing skill to the authenticated mentor's offered skills list (PUT).
     * ================================================================ */
    @PutMapping("/profile/skills/{skillId}")
    public String addSkillPut(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.addSkillToMentor(userId, skillId);
    }

    /* ================================================================
     * METHOD: addSkillPost
     * DESCRIPTION:
     * Adds an existing skill to the authenticated mentor's offered skills list (POST).
     * ================================================================ */
    @PostMapping("/profile/skills/{skillId}")
    public String addSkillPost(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.addSkillToMentor(userId, skillId);
    }

    /* ================================================================
     * METHOD: removeSkill
     * DESCRIPTION:
     * Removes an existing skill from the authenticated mentor's offered skills list.
     * ================================================================ */
    @DeleteMapping("/profile/skills/{skillId}")
    public String removeSkill(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.removeSkillFromMentor(userId, skillId);
    }

    /* ================================================================
     * METHOD: searchMentors
     * DESCRIPTION:
     * Multi-field search mapping sorting via price, rating, or availability.
     * ================================================================ */
    @GetMapping("/search")
    public Page<Mentor> searchMentors(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Boolean available,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "averageRating") String sortBy) {

        return mentorService.searchMentors(
                minPrice, maxPrice, rating, available, page, size, sortBy);
    }

    /* ================================================================
     * METHOD: searchByPrice
     * DESCRIPTION:
     * Facilitates filtering mentors mapped to a specific hourly rate range.
     * ================================================================ */
    @GetMapping("/search/price")
    public Page<Mentor> searchByPrice(
            @RequestParam Double min,
            @RequestParam Double max,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "hourlyRate") String sortBy) {

        return mentorService.searchByPrice(min, max, page, size, sortBy);
    }

    /* ================================================================
     * METHOD: searchByRating
     * DESCRIPTION:
     * Retrieves mentors starting out with a rating above a set threshold.
     * ================================================================ */
    @GetMapping("/search/rating")
    public Page<Mentor> searchByRating(
            @RequestParam Double rating,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "averageRating") String sortBy) {

        return mentorService.searchByRating(rating, page, size, sortBy);
    }

    /* ================================================================
     * METHOD: searchBySkill
     * DESCRIPTION:
     * Filters mentors explicitly based on a provided global skill ID.
     * ================================================================ */
    @GetMapping("/search/skill/{skillId}")
    public Page<Mentor> searchBySkill(
            @PathVariable Long skillId,
            @RequestParam int page,
            @RequestParam int size) {

        return mentorService.searchMentorBySkill(skillId, page, size);
    }

    /* ================================================================
     * METHOD: mentorExists
     * DESCRIPTION:
     * Evaluates whether a generic mentor ID matches an active mentor via Feign.
     * ================================================================ */
    @GetMapping("/exists/{mentorId}")
    public Boolean mentorExists(@PathVariable Long mentorId) {
        return mentorService.mentorExists(mentorId);
    }

    /* ================================================================
     * METHOD: getUserIdByMentorId
     * DESCRIPTION:
     * Retrieves the Global User ID representing a mentor for internal checks.
     * ================================================================ */
    @GetMapping("/internal/{mentorId}/userid")
    public Long getUserIdByMentorId(@PathVariable Long mentorId) {
        return mentorService.getUserIdByMentorId(mentorId);
    }
    
    /* ================================================================
     * METHOD: getMentorIdByUserId
     * DESCRIPTION:
     * Resolves the system's Mentor ID assigned explicitly to the User ID.
     * ================================================================ */
    @GetMapping("/by-user/{id}")
    public Long getMentorIdByUserId(@PathVariable("id") Long userId) {
        return mentorService.getMentorIdByUserId(userId);
    }
    
    @io.swagger.v3.oas.annotations.Hidden
    @PutMapping("/{mentorId}/rating")
    public String updateRating(
            @PathVariable Long mentorId,
            @RequestParam Double rating,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {

        if (!"internal_secret_key_123".equals(secret)) {
            throw new RuntimeException("Direct external access to this internal API is strictly forbidden.");
        }

        mentorService.updateRating(mentorId, rating);
        return "Rating updated";
    }
    
}