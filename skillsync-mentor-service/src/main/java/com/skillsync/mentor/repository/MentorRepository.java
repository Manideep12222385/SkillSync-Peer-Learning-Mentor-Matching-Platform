package com.skillsync.mentor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorStatus;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    Optional<Mentor> findByUserId(Long userId);

    List<Mentor> findByStatus(MentorStatus status);
}