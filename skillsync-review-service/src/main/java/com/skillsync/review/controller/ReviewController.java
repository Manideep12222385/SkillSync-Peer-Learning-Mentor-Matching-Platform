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

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: ReviewController
 * DESCRIPTION:
 * This controller handles review-related operations such as submitting,
 * editing, deleting, and fetching reviews for mentors and learners.
 * ================================================================
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // ⭐ SUBMIT REVIEW (SECURED)
    /* ================================================================
     * METHOD: submit
     * DESCRIPTION:
     * Submits a new review for a mentor from an authenticated learner.
     * ================================================================ */
    @PostMapping
    public Review submit(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();

        return service.submitReview(request, learnerId);
    }

    /* ================================================================
     * METHOD: mentorReviews
     * DESCRIPTION:
     * Fetches all reviews submitted for a specific mentor.
     * ================================================================ */
    @GetMapping("/mentor/{mentorId}")
    public List<Review> mentorReviews(@PathVariable Long mentorId) {
        return service.getMentorReviews(mentorId);
    }

    /* ================================================================
     * METHOD: averageRating
     * DESCRIPTION:
     * Calculates and returns the average rating for a specific mentor.
     * ================================================================ */
    @GetMapping("/mentor/{mentorId}/average")
    public Double averageRating(@PathVariable Long mentorId) {
        return service.getAverageRating(mentorId);
    }

    /* ================================================================
     * METHOD: learnerReviews
     * DESCRIPTION:
     * Fetches all reviews written by a specific learner.
     * ================================================================ */
    @GetMapping("/learner/{learnerId}")
    public List<Review> learnerReviews(@PathVariable Long learnerId) {
        return service.getLearnerReviews(learnerId);
    }

    /* ================================================================
     * METHOD: editReview
     * DESCRIPTION:
     * Edits an existing review if the currently authenticated logged 
     * learner is the author.
     * ================================================================ */
    @PutMapping("/{id}")
    public Review editReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.editReview(id, request, learnerId);
    }

    /* ================================================================
     * METHOD: deleteReview
     * DESCRIPTION:
     * Deletes a review from the system if the currently authenticated 
     * logged learner is the author.
     * ================================================================ */
    @DeleteMapping("/{id}")
    public String deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
            
        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        service.deleteReview(id, learnerId);
        return "Review deleted successfully";
    }
}