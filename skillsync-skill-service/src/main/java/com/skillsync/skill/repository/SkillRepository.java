package com.skillsync.skill.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillsync.skill.entity.Skill;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findBySkillName(String skillName);

    Page<Skill> findByActiveTrue(Pageable pageable);

    Page<Skill> findBySkillNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
    
    Boolean existsBySkillIdAndActiveTrue(Long skillId);
}