package com.skillsync.review.controller;

import com.skillsync.review.entity.Review;
import com.skillsync.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // ⭐ SUBMIT REVIEW
    @PostMapping
    public Review submit(@RequestBody Review review) {
        return service.submitReview(review);
    }

    // ⭐ GET REVIEWS FOR MENTOR
    @GetMapping("/mentor/{mentorId}")
    public List<Review> mentorReviews(
            @PathVariable Long mentorId) {

        return service.getMentorReviews(mentorId);
    }

    // ⭐ GET AVG RATING
    @GetMapping("/mentor/{mentorId}/average")
    public Double averageRating(
            @PathVariable Long mentorId) {

        return service.getAverageRating(mentorId);
    }

    // ⭐ GET REVIEWS BY LEARNER
    @GetMapping("/learner/{learnerId}")
    public List<Review> learnerReviews(
            @PathVariable Long learnerId) {

        return service.getLearnerReviews(learnerId);
    }

    // ⭐ DELETE REVIEW
    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable Long id) {
        service.deleteReview(id);
        return "Review deleted successfully";
    }
}