package com.skillsync.mentor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.skillsync.mentor.dto.*;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.security.JwtUtil;
import com.skillsync.mentor.service.MentorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mentors")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/profile")
    public MentorProfileResponseDto createProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateMentorProfileRequestDto request) {

        Long userId = jwtUtil.extractUserId(token);
        request.setUserId(userId);

        return mentorService.createProfile(request);
    }

    @PutMapping("/{mentorId}")
    public MentorProfileResponseDto updateProfile(
            @PathVariable Long mentorId,
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateMentorProfileRequestDto request) {

        return mentorService.updateProfile(mentorId, request);
    }

    @PostMapping("/{mentorId}/skills/{skillId}")
    public String addSkill(
            @PathVariable Long mentorId,
            @PathVariable Long skillId) {

        return mentorService.addSkillToMentor(mentorId, skillId);
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

    @PutMapping("/{mentorId}/rating")
    public String updateRating(
            @PathVariable Long mentorId,
            @RequestParam Double rating) {

        mentorService.updateRating(mentorId, rating);
        return "Rating updated";
    }

    @PutMapping("/admin/{mentorId}/approve")
    public String approve(@PathVariable Long mentorId) {
        return mentorService.approveMentor(mentorId);
    }

    @PutMapping("/admin/{mentorId}/reject")
    public String reject(@PathVariable Long mentorId) {
        return mentorService.rejectMentor(mentorId);
    }
}