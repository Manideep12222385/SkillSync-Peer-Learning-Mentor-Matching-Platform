package com.skillsync.skill.service;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import com.skillsync.skill.dto.*;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.repository.SkillRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @CacheEvict(value = "skills", allEntries = true)
    public SkillResponseDto createSkill(CreateSkillRequestDto request) {
        log.info("Creating skill with name: {}", request.getSkillName());

        skillRepository.findBySkillName(request.getSkillName())
                .ifPresent(s -> {
                    log.error("Skill already exists: {}", request.getSkillName());
                    throw new RuntimeException("Skill already exists");
                });

        Skill skill = Skill.builder()
                .skillName(request.getSkillName())
                .category(request.getCategory())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        skillRepository.save(skill);

        return map(skill, "Skill created");
    }

    @CacheEvict(value = "skills", allEntries = true)
    public SkillResponseDto updateSkill(
            Long skillId,
            UpdateSkillRequestDto request) {
        log.info("Updating skill with id: {}", skillId);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> {
                    log.error("Skill not found for id: {}", skillId);
                    return new RuntimeException("Skill not found");
                });

        if (request.getSkillName() != null)
            skill.setSkillName(request.getSkillName());

        if (request.getCategory() != null)
            skill.setCategory(request.getCategory());

        if (request.getActive() != null)
            skill.setActive(request.getActive());

        skillRepository.save(skill);

        return map(skill, "Skill updated");
    }

    @CacheEvict(value = "skills", allEntries = true)
    public String deleteSkill(Long skillId) {
        log.info("Deleting skill with id: {}", skillId);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> {
                    log.error("Skill not found for id: {}", skillId);
                    return new RuntimeException("Skill not found");
                });

        skillRepository.delete(skill);

        return "Skill deleted";
    }

    @Cacheable(value = "skills")
    public Page<Skill> getAllActiveSkills(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return skillRepository.findByActiveTrue(pageable);
    }

    @Cacheable(value = "skills")
    public Page<Skill> searchSkills(
            String keyword,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        return skillRepository
                .findBySkillNameContainingIgnoreCase(keyword, pageable);
    }

    private SkillResponseDto map(Skill skill, String msg) {

        return SkillResponseDto.builder()
                .skillId(skill.getSkillId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory())
                .active(skill.getActive())
                .message(msg)
                .build();
    }
    
    @Cacheable(value = "skills")
    public Boolean skillExists(Long skillId) {
        return skillRepository.existsBySkillIdAndActiveTrue(skillId);
    }
}