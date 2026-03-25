package com.skillsync.skill.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.skillsync.skill.dto.*;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.repository.SkillRepository;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public SkillResponseDto createSkill(CreateSkillRequestDto request) {

        skillRepository.findBySkillName(request.getSkillName())
                .ifPresent(s -> {
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

    public SkillResponseDto updateSkill(
            Long skillId,
            UpdateSkillRequestDto request) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (request.getSkillName() != null)
            skill.setSkillName(request.getSkillName());

        if (request.getCategory() != null)
            skill.setCategory(request.getCategory());

        if (request.getActive() != null)
            skill.setActive(request.getActive());

        skillRepository.save(skill);

        return map(skill, "Skill updated");
    }

    public String deleteSkill(Long skillId) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        skillRepository.delete(skill);

        return "Skill deleted";
    }

    public Page<Skill> getAllActiveSkills(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return skillRepository.findByActiveTrue(pageable);
    }

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
    
    public Boolean skillExists(Long skillId) {
        return skillRepository.existsBySkillIdAndActiveTrue(skillId);
    }
}