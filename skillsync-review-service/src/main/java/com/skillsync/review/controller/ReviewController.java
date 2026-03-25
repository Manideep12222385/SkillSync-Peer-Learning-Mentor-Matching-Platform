package com.skillsync.review.controller;

import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.entity.Review;
import com.skillsync.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // ⭐ SUBMIT REVIEW (SECURED)
    @PostMapping
    public Review submit(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = jwt.getClaim("userId");

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

    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable Long id) {
        service.deleteReview(id);
        return "Review deleted successfully";
    }
}