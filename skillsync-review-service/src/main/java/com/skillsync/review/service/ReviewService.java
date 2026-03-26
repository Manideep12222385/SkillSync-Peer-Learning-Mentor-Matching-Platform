package com.skillsync.review.service;

import com.skillsync.review.client.MentorClient;
import com.skillsync.review.client.SessionClient;
import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.SessionDTO;
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
	public Review submitReview(ReviewRequest request, Long learnerId) {

	    SessionDTO session;

	    try {
	        session = sessionClient.getSession(request.getSessionId());
	    }
	    catch (Exception ex) {
	        throw new RuntimeException("Session not found or invalid session id");
	    }

	    if (session == null) {
	        throw new RuntimeException("Session not found");
	    }

	    if (!session.getLearnerId().equals(learnerId)) {
	        throw new RuntimeException("You can review only your own sessions");
	    }

	    if (!"COMPLETED".equalsIgnoreCase(session.getStatus())) {
	        throw new RuntimeException("Review allowed only after session completion");
	    }

	    if (repository.existsBySessionId(request.getSessionId())) {
	        throw new RuntimeException("Review already submitted for this session");
	    }

	    Integer rating = request.getRating();

	    Review review = Review.builder()
	            .mentorId(session.getMentorId())
	            .learnerId(learnerId)
	            .sessionId(request.getSessionId())
	            .rating(rating)
	            .comment(request.getComment())
	            .createdAt(LocalDateTime.now())
	            .build();

	    Review saved = repository.save(review);

	    Double avg = getAverageRating(session.getMentorId());

	    mentorClient.updateMentorRating(session.getMentorId(), avg, "internal_secret_key_123");

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

	// ⭐ EDIT REVIEW
	public Review editReview(Long id, ReviewRequest request, Long learnerId) {

		Review review = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Review not found"));

		if (!review.getLearnerId().equals(learnerId)) {
			throw new RuntimeException("You can only edit your own reviews");
		}

		review.setRating(request.getRating());
		review.setComment(request.getComment());

		Review updated = repository.save(review);

		Double avg = getAverageRating(review.getMentorId());
		mentorClient.updateMentorRating(review.getMentorId(), avg, "internal_secret_key_123");

		return updated;
	}

	// ⭐ DELETE REVIEW
	public void deleteReview(Long id, Long learnerId) {

		Review review = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Review not found"));

		if (!review.getLearnerId().equals(learnerId)) {
			throw new RuntimeException("You can only delete your own reviews");
		}

		repository.deleteById(id);

		Double avg = getAverageRating(review.getMentorId());
		mentorClient.updateMentorRating(review.getMentorId(), avg, "internal_secret_key_123");
	}
}