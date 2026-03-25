package com.skillsync.mentor.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorStatus;

public interface MentorRepository
        extends JpaRepository<Mentor, Long>,
                JpaSpecificationExecutor<Mentor> {

    boolean existsByUserId(Long userId);

    // ⭐ search by price + approved
    Page<Mentor> findByHourlyRateBetweenAndStatus(
            Double min,
            Double max,
            MentorStatus status,
            Pageable pageable
    );

    // ⭐ search by rating + approved
    Page<Mentor> findByAverageRatingGreaterThanEqualAndStatus(
            Double rating,
            MentorStatus status,
            Pageable pageable
    );

    // ⭐ search by mentorIds + approved
    Page<Mentor> findByMentorIdInAndStatus(
            List<Long> mentorIds,
            MentorStatus status,
            Pageable pageable
    );
}