package com.skillsync.review.service;

import com.skillsync.review.client.MentorClient;
import com.skillsync.review.client.SessionClient;
import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.SessionDTO;
import com.skillsync.review.entity.Review;
import com.skillsync.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SessionClient sessionClient;

    @Mock
    private MentorClient mentorClient;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewRequest reviewRequest;
    private SessionDTO sessionDTO;
    private Review testReview;

    @BeforeEach
    void setUp() {
        reviewRequest = new ReviewRequest();
        reviewRequest.setSessionId(1L);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great session!");

        sessionDTO = new SessionDTO();
        sessionDTO.setId(1L);
        sessionDTO.setMentorId(10L);
        sessionDTO.setLearnerId(20L);
        sessionDTO.setStatus("COMPLETED");

        testReview = Review.builder()
                .id(1L)
                .mentorId(10L)
                .learnerId(20L)
                .sessionId(1L)
                .rating(5)
                .comment("Great session!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- SUBMIT REVIEW TESTS ---
    @Test
    void testSubmitReview_Success() {
        when(sessionClient.getSession(1L)).thenReturn(sessionDTO);
        when(reviewRepository.existsBySessionId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        // getAverageRating is called internally:
        when(reviewRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(testReview));

        Review response = reviewService.submitReview(reviewRequest, 20L);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(mentorClient, times(1)).updateMentorRating(eq(10L), eq(5.0), eq("internal_secret_key_123"));
    }

    @Test
    void testSubmitReview_SessionClientException() {
        when(sessionClient.getSession(1L)).thenThrow(new RuntimeException("Feign client error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.submitReview(reviewRequest, 20L));
        assertEquals("Session not found or invalid session id", ex.getMessage());
    }

    @Test
    void testSubmitReview_SessionNull() {
        when(sessionClient.getSession(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.submitReview(reviewRequest, 20L));
        assertEquals("Session not found", ex.getMessage());
    }

    @Test
    void testSubmitReview_NotYourSession() {
        when(sessionClient.getSession(1L)).thenReturn(sessionDTO);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.submitReview(reviewRequest, 99L));
        assertEquals("You can review only your own sessions", ex.getMessage());
    }

    @Test
    void testSubmitReview_NotCompleted() {
        sessionDTO.setStatus("REQUESTED");
        when(sessionClient.getSession(1L)).thenReturn(sessionDTO);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.submitReview(reviewRequest, 20L));
        assertEquals("Review allowed only after session completion", ex.getMessage());
    }

    @Test
    void testSubmitReview_AlreadySubmitted() {
        when(sessionClient.getSession(1L)).thenReturn(sessionDTO);
        when(reviewRepository.existsBySessionId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.submitReview(reviewRequest, 20L));
        assertEquals("Review already submitted for this session", ex.getMessage());
    }

    // --- GET METHODS TESTS ---
    @Test
    void testGetMentorReviews() {
        when(reviewRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(testReview));
        List<Review> reviews = reviewService.getMentorReviews(10L);
        assertEquals(1, reviews.size());
    }

    @Test
    void testGetLearnerReviews() {
        when(reviewRepository.findByLearnerId(20L)).thenReturn(Collections.singletonList(testReview));
        List<Review> reviews = reviewService.getLearnerReviews(20L);
        assertEquals(1, reviews.size());
    }

    @Test
    void testGetAverageRating_Empty() {
        when(reviewRepository.findByMentorId(10L)).thenReturn(Collections.emptyList());
        Double avg = reviewService.getAverageRating(10L);
        assertEquals(0.0, avg);
    }

    @Test
    void testGetAverageRating_Multiple() {
        Review r2 = Review.builder().rating(3).build();
        when(reviewRepository.findByMentorId(10L)).thenReturn(Arrays.asList(testReview, r2));
        
        Double avg = reviewService.getAverageRating(10L);
        assertEquals(4.0, avg); // (5+3) / 2 = 4.0
    }

    // --- EDIT REVIEW TESTS ---
    @Test
    void testEditReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        // Internally calls getAverageRating
        when(reviewRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(testReview));

        ReviewRequest updateRequest = new ReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setComment("Edited comment");

        Review response = reviewService.editReview(1L, updateRequest, 20L);

        assertNotNull(response);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(mentorClient, times(1)).updateMentorRating(eq(10L), eq(4.0), eq("internal_secret_key_123"));
    }

    @Test
    void testEditReview_NotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewRequest updateRequest = new ReviewRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.editReview(1L, updateRequest, 20L));
        assertEquals("Review not found", ex.getMessage());
    }

    @Test
    void testEditReview_NotYourReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        ReviewRequest updateRequest = new ReviewRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.editReview(1L, updateRequest, 99L));
        assertEquals("You can only edit your own reviews", ex.getMessage());
    }

    // --- DELETE REVIEW TESTS ---
    @Test
    void testDeleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.findByMentorId(10L)).thenReturn(Collections.emptyList());

        reviewService.deleteReview(1L, 20L);

        verify(reviewRepository, times(1)).deleteById(1L);
        verify(mentorClient, times(1)).updateMentorRating(eq(10L), eq(0.0), eq("internal_secret_key_123"));
    }

    @Test
    void testDeleteReview_NotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.deleteReview(1L, 20L));
        assertEquals("Review not found", ex.getMessage());
    }

    @Test
    void testDeleteReview_NotYourReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.deleteReview(1L, 99L));
        assertEquals("You can only delete your own reviews", ex.getMessage());
    }
}
