package com.skillsync.mentor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.skillsync.mentor.dto.*;
import com.skillsync.mentor.entity.*;
import com.skillsync.mentor.feign.SkillClient;
import com.skillsync.mentor.repository.MentorRepository;
import com.skillsync.mentor.repository.MentorSkillRepository;
import com.skillsync.mentor.specification.MentorSpecification;
import com.skillsync.mentor.specification.MentorSearchSpecification;

@Service
public class MentorService {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private MentorSkillRepository mentorSkillRepository;

    @Autowired
    private SkillClient skillClient;

    // ⭐ CREATE PROFILE (PENDING)
    public MentorProfileResponseDto createProfile(CreateMentorProfileRequestDto request) {

        if (mentorRepository.existsByUserId(request.getUserId()))
            throw new RuntimeException("Mentor profile already exists");

        if (request.getHourlyRate() < 0)
            throw new RuntimeException("Hourly rate cannot be negative");

        if (request.getExperienceYears() < 0)
            throw new RuntimeException("Experience cannot be negative");

        Mentor mentor = Mentor.builder()
                .userId(request.getUserId())
                .bio(request.getBio())
                .experienceYears(request.getExperienceYears())
                .hourlyRate(request.getHourlyRate())
                .averageRating(0.0)
                .totalSessions(0)
                .available(request.getAvailable())
                .createdAt(LocalDateTime.now())
                .status(MentorStatus.PENDING)
                .build();

        mentorRepository.save(mentor);

        return map(mentor, "Profile created. Waiting for admin approval");
    }

    // ⭐ UPDATE PROFILE
    public MentorProfileResponseDto updateProfile(Long mentorId,
                                                  UpdateMentorProfileRequestDto request) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        if (mentor.getStatus() != MentorStatus.APPROVED)
            throw new RuntimeException("Mentor not approved yet");

        if (request.getBio() != null)
            mentor.setBio(request.getBio());

        if (request.getExperienceYears() != null && request.getExperienceYears() >= 0)
            mentor.setExperienceYears(request.getExperienceYears());

        if (request.getHourlyRate() != null && request.getHourlyRate() >= 0)
            mentor.setHourlyRate(request.getHourlyRate());

        if (request.getAvailable() != null)
            mentor.setAvailable(request.getAvailable());

        mentorRepository.save(mentor);

        return map(mentor, "Profile updated");
    }

    // ⭐ ADD SKILL
    public String addSkillToMentor(Long mentorId, Long skillId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        if (mentor.getStatus() != MentorStatus.APPROVED)
            throw new RuntimeException("Mentor not approved yet");

        SkillDto skill = skillClient.getSkillById(skillId);

        if (!skill.getActive())
            throw new RuntimeException("Skill is inactive");

        boolean exists = mentorSkillRepository
                .findByMentorId(mentorId)
                .stream()
                .anyMatch(m -> m.getSkillId().equals(skillId));

        if (exists)
            throw new RuntimeException("Skill already added");

        MentorSkill mapping = MentorSkill.builder()
                .mentorId(mentorId)
                .skillId(skillId)
                .build();

        mentorSkillRepository.save(mapping);

        return "Skill added";
    }

    // ⭐ SEARCH BY PRICE
    public Page<Mentor> searchByPrice(Double min, Double max,
                                      int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return mentorRepository
                .findByHourlyRateBetweenAndStatus(min, max,
                        MentorStatus.APPROVED, pageable);
    }

    // ⭐ SEARCH BY RATING
    public Page<Mentor> searchByRating(Double rating,
                                       int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return mentorRepository
                .findByAverageRatingGreaterThanEqualAndStatus(
                        rating, MentorStatus.APPROVED, pageable);
    }

    // ⭐ SEARCH BY SKILL
    public Page<Mentor> searchMentorBySkill(Long skillId,
                                            int page, int size) {

        List<MentorSkill> mappings =
                mentorSkillRepository.findBySkillId(skillId);

        List<Long> mentorIds =
                mappings.stream().map(MentorSkill::getMentorId).toList();

        Pageable pageable = PageRequest.of(page, size);

        return mentorRepository
                .findByMentorIdInAndStatus(mentorIds,
                        MentorStatus.APPROVED, pageable);
    }

    // ⭐ COMBINED SEARCH
    public Page<Mentor> searchMentors(Double minPrice,
                                      Double maxPrice,
                                      Double rating,
                                      Boolean available,
                                      int page,
                                      int size,
                                      String sortBy) {

        Specification<Mentor> spec =
                Specification.where(MentorSpecification.hasMinPrice(minPrice))
                        .and(MentorSpecification.hasMaxPrice(maxPrice))
                        .and(MentorSpecification.hasRating(rating))
                        .and(MentorSpecification.isAvailable(available))
                        .and((root, q, cb) ->
                                cb.equal(root.get("status"),
                                        MentorStatus.APPROVED));

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortBy).descending());

        return mentorRepository.findAll(spec, pageable);
    }

    // ⭐ ADVANCED SEARCH
    public Page<Mentor> searchMentorsAdvanced(Long skillId,
                                              Double minPrice,
                                              Double maxPrice,
                                              Double rating,
                                              Boolean available,
                                              int page,
                                              int size,
                                              String sortBy) {

        Specification<Mentor> spec =
                Specification.where(MentorSearchSpecification.hasSkill(skillId))
                        .and(MentorSearchSpecification.minPrice(minPrice))
                        .and(MentorSearchSpecification.maxPrice(maxPrice))
                        .and(MentorSearchSpecification.minRating(rating))
                        .and(MentorSearchSpecification.availability(available))
                        .and((root, q, cb) ->
                                cb.equal(root.get("status"),
                                        MentorStatus.APPROVED));

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortBy).descending());

        return mentorRepository.findAll(spec, pageable);
    }

    // ⭐ UPDATE RATING
    public void updateRating(Long mentorId, Double rating) {

        if (rating < 0 || rating > 5)
            throw new RuntimeException("Invalid rating");

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        double avg = mentor.getAverageRating();
        int total = mentor.getTotalSessions();

        double newAvg = ((avg * total) + rating) / (total + 1);

        mentor.setAverageRating(newAvg);
        mentor.setTotalSessions(total + 1);

        mentorRepository.save(mentor);
    }

    // ⭐ ADMIN APPROVE
    public String approveMentor(Long mentorId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        if (mentor.getStatus() == MentorStatus.APPROVED)
            throw new RuntimeException("Already approved");

        mentor.setStatus(MentorStatus.APPROVED);
        mentorRepository.save(mentor);

        return "Mentor approved";
    }

    // ⭐ ADMIN REJECT
    public String rejectMentor(Long mentorId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        mentor.setStatus(MentorStatus.REJECTED);
        mentor.setAvailable(false);

        mentorRepository.save(mentor);

        return "Mentor rejected";
    }

    public Boolean mentorExists(Long mentorId) {
        return mentorRepository.existsById(mentorId);
    }

    private MentorProfileResponseDto map(Mentor mentor, String msg) {

        return MentorProfileResponseDto.builder()
                .mentorId(mentor.getMentorId())
                .userId(mentor.getUserId())
                .bio(mentor.getBio())
                .experienceYears(mentor.getExperienceYears())
                .hourlyRate(mentor.getHourlyRate())
                .averageRating(mentor.getAverageRating())
                .totalSessions(mentor.getTotalSessions())
                .available(mentor.getAvailable())
                .message(msg)
                .build();
    }
}