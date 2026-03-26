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

@RestController
@RequestMapping("/mentors")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/profile")
    public MentorProfileResponseDto createProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateMentorProfileRequestDto request) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.createProfile(userId, request);
    }

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

    @PutMapping("/profile/skills/{skillId}")
    public String addSkillPut(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.addSkillToMentor(userId, skillId);
    }

    @PostMapping("/profile/skills/{skillId}")
    public String addSkillPost(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        Long userId = jwtUtil.extractUserId(token);

        return mentorService.addSkillToMentor(userId, skillId);
    }

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

    @GetMapping("/search/price")
    public Page<Mentor> searchByPrice(
            @RequestParam Double min,
            @RequestParam Double max,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "hourlyRate") String sortBy) {

        return mentorService.searchByPrice(min, max, page, size, sortBy);
    }

    @GetMapping("/search/rating")
    public Page<Mentor> searchByRating(
            @RequestParam Double rating,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "averageRating") String sortBy) {

        return mentorService.searchByRating(rating, page, size, sortBy);
    }

    @GetMapping("/search/skill/{skillId}")
    public Page<Mentor> searchBySkill(
            @PathVariable Long skillId,
            @RequestParam int page,
            @RequestParam int size) {

        return mentorService.searchMentorBySkill(skillId, page, size);
    }

    @GetMapping("/exists/{mentorId}")
    public Boolean mentorExists(@PathVariable Long mentorId) {
        return mentorService.mentorExists(mentorId);
    }

    @GetMapping("/internal/{mentorId}/userid")
    public Long getUserIdByMentorId(@PathVariable Long mentorId) {
        return mentorService.getUserIdByMentorId(mentorId);
    }
    
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