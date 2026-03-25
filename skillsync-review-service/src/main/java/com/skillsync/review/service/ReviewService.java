package com.skillsync.review.service;

import com.skillsync.review.client.MentorClient;
import com.skillsync.review.client.SessionClient;
import com.skillsync.review.entity.Review;
import com.skillsync.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository repository;
	private final SessionClient sessionClient;
	private final MentorClient mentorClient;

	// ⭐ SUBMIT REVIEW
	public Review submitReview(Review review) {

		Boolean completed = sessionClient.isSessionCompleted(review.getSessionId());

		if (completed == null || !completed) {
			throw new RuntimeException("Review allowed only after session completion");
		}

		if (review.getRating() < 1 || review.getRating() > 5) {
			throw new RuntimeException("Rating must be between 1 and 5");
		}

		review.setCreatedAt(LocalDateTime.now());

		Review saved = repository.save(review);

		// ⭐ calculate avg rating
		Double avg = getAverageRating(review.getMentorId());

		// ⭐ call mentor service
		mentorClient.updateMentorRating(review.getMentorId(), avg);

		return saved;
	}

	// ⭐ GET ALL REVIEWS FOR MENTOR
	public List<Review> getMentorReviews(Long mentorId) {
		return repository.findByMentorId(mentorId);
	}

	// ⭐ GET ALL REVIEWS BY LEARNER
	public List<Review> getLearnerReviews(Long learnerId) {
		return repository.findByLearnerId(learnerId);
	}

	// ⭐ GET AVERAGE RATING
	public Double getAverageRating(Long mentorId) {

		List<Review> reviews = repository.findByMentorId(mentorId);

		if (reviews.isEmpty())
			return 0.0;

		double sum = 0;
		for (Review r : reviews) {
			sum += r.getRating();
		}

		return sum / reviews.size();
	}

	// ⭐ DELETE REVIEW
	public void deleteReview(Long id) {

		if (!repository.existsById(id)) {
			throw new RuntimeException("Review not found");
		}

		repository.deleteById(id);
	}
}