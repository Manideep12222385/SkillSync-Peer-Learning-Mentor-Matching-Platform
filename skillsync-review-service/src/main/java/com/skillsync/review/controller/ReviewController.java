package com.skillsync.review.controller;

import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.entity.Review;
import com.skillsync.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // ⭐ SUBMIT REVIEW (SECURED)
    @PostMapping
    public Review submit(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();

        return service.submitReview(request, learnerId);
    }

    @GetMapping("/mentor/{mentorId}")
    public List<Review> mentorReviews(@PathVariable Long mentorId) {
        return service.getMentorReviews(mentorId);
    }

    @GetMapping("/mentor/{mentorId}/average")
    public Double averageRating(@PathVariable Long mentorId) {
        return service.getAverageRating(mentorId);
    }

    @GetMapping("/learner/{learnerId}")
    public List<Review> learnerReviews(@PathVariable Long learnerId) {
        return service.getLearnerReviews(learnerId);
    }

    @PutMapping("/{id}")
    public Review editReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.editReview(id, request, learnerId);
    }

    @DeleteMapping("/{id}")
    public String deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
            
        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        service.deleteReview(id, learnerId);
        return "Review deleted successfully";
    }
}