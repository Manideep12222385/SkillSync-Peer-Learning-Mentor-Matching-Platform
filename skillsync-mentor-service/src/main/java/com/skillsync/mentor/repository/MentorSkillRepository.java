package com.skillsync.mentor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.mentor.entity.MentorSkill;

public interface MentorSkillRepository
        extends JpaRepository<MentorSkill, Long> {

    List<MentorSkill> findBySkillId(Long skillId);

    List<MentorSkill> findByMentorId(Long mentorId);
}