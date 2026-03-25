package com.skillsync.review.repository;

import com.skillsync.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMentorId(Long mentorId);

    List<Review> findByLearnerId(Long learnerId);
    
    boolean existsBySessionId(Long sessionId);
    
}