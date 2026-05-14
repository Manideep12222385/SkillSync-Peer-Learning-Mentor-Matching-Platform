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

import lombok.extern.slf4j.Slf4j;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: MentorService
 * DESCRIPTION:
 * Handles mentor profile administration incorporating multi-filter
 * JPA Specification queries for mentor searching mapped with ratings.
 * ================================================================
 */
@Service
@Slf4j
public class MentorService {

	@Autowired
	private MentorRepository mentorRepository;

	@Autowired
	private MentorSkillRepository mentorSkillRepository;

	@Autowired
	private SkillClient skillClient;

	/* ================================================================
	 * METHOD: createProfile
	 * DESCRIPTION:
	 * Establishes a brand-new mentor profile assigned to the provided user ID.
	 * ================================================================ */
	public MentorProfileResponseDto createProfile(Long userId, CreateMentorProfileRequestDto request) {
		log.info("Creating mentor profile for user id: {}", userId);

		if (mentorRepository.existsByUserId(userId)) {
			log.error("Mentor profile already exists for user id: {}", userId);
			throw new RuntimeException("Mentor profile already exists");
		}

		if (request.getHourlyRate() < 0) {
			log.error("Hourly rate cannot be negative for user id: {}", userId);
			throw new RuntimeException("Hourly rate cannot be negative");
		}

		if (request.getExperienceYears() < 0) {
			log.error("Experience cannot be negative for user id: {}", userId);
			throw new RuntimeException("Experience cannot be negative");
		}

		Mentor mentor = Mentor.builder().userId(userId).bio(request.getBio())
				.experienceYears(request.getExperienceYears()).hourlyRate(request.getHourlyRate()).averageRating(0.0)
				.totalSessions(0).available(request.getAvailable()).createdAt(LocalDateTime.now())
				.status(MentorStatus.APPROVED).profileImage(request.getProfileImage())
				.upiId(request.getUpiId()).qrCodeImage(request.getQrCodeImage()).build();

		mentorRepository.save(mentor);

		return map(mentor, "Profile created successfully.");
	}

	/* ================================================================
	 * METHOD: updateProfile
	 * DESCRIPTION:
	 * Extends and updates properties applied natively to approved mentors.
	 * ================================================================ */
	public MentorProfileResponseDto getProfile(Long userId) {
		log.info("Fetching mentor profile for user id: {}", userId);
		Mentor mentor = mentorRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Mentor profile not found"));
		return map(mentor, "Profile fetched successfully.");
	}

	/* ================================================================
	 * METHOD: updateProfile
	 * DESCRIPTION:
	 * Updates an existing mentor profile with newer data such as bio,
	 * experience, pricing, etc.
	 * ================================================================ */
	public MentorProfileResponseDto updateProfile(Long userId, UpdateMentorProfileRequestDto request) {
		log.info("Updating mentor profile for user id: {}", userId);

		Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> {
					log.error("Mentor profile not found for user id: {}", userId);
					return new RuntimeException("Mentor profile not found");
				});

		if (mentor.getStatus() != MentorStatus.APPROVED) {
			log.error("Mentor not approved yet for user id: {}", userId);
			throw new RuntimeException("Mentor not approved yet");
		}

		if (request.getBio() != null)
			mentor.setBio(request.getBio());

		if (request.getExperienceYears() != null && request.getExperienceYears() >= 0)
			mentor.setExperienceYears(request.getExperienceYears());

		if (request.getHourlyRate() != null && request.getHourlyRate() >= 0)
			mentor.setHourlyRate(request.getHourlyRate());

		if (request.getAvailable() != null)
			mentor.setAvailable(request.getAvailable());

		if (request.getProfileImage() != null)
			mentor.setProfileImage(request.getProfileImage());

		if (request.getUpiId() != null)
			mentor.setUpiId(request.getUpiId());

		if (request.getQrCodeImage() != null)
			mentor.setQrCodeImage(request.getQrCodeImage());

		mentorRepository.save(mentor);

		return map(mentor, "Profile updated");
	}

	/* ================================================================
	 * METHOD: addSkillToMentor
	 * DESCRIPTION:
	 * Adds an externally validated skill mapping to the mentor's profile.
	 * ================================================================ */
	public String addSkillToMentor(Long userId, Long skillId) {
		log.info("Adding skill id: {} to mentor user id: {}", skillId, userId);

		Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> {
					log.error("Mentor profile not found for user id: {}", userId);
					return new RuntimeException("Mentor profile not found");
				});
                
        Long mentorId = mentor.getMentorId();

		if (mentor.getStatus() != MentorStatus.APPROVED)
			throw new RuntimeException("Mentor not approved yet");

		Boolean isActive = skillClient.skillExists(skillId);

		if (isActive == null || !isActive)
			throw new RuntimeException("Skill is inactive or not found");

		boolean exists = mentorSkillRepository.findByMentorId(mentorId).stream()
				.anyMatch(m -> m.getSkillId().equals(skillId));

		if (exists)
			throw new RuntimeException("Skill already added");

		MentorSkill mapping = MentorSkill.builder().mentorId(mentorId).skillId(skillId).build();

		mentorSkillRepository.save(mapping);

		return "Skill added";
	}

	/* ================================================================
	 * METHOD: removeSkillFromMentor
	 * DESCRIPTION:
	 * Removes a skill mapping from the mentor's profile.
	 * ================================================================ */
	public String removeSkillFromMentor(Long userId, Long skillId) {
		log.info("Removing skill id: {} from mentor user id: {}", skillId, userId);

		Mentor mentor = mentorRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Mentor profile not found"));

		Long mentorId = mentor.getMentorId();

		MentorSkill mapping = mentorSkillRepository.findByMentorId(mentorId).stream()
				.filter(m -> m.getSkillId().equals(skillId))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Skill not found in mentor's profile"));

		mentorSkillRepository.delete(mapping);

		return "Skill removed";
	}

	/* ================================================================
	 * METHOD: searchByPrice
	 * DESCRIPTION:
	 * Paged search algorithm focused on filtering mentors through hourly rates.
	 * ================================================================ */
	public Page<Mentor> searchByPrice(Double min, Double max, int page, int size, String sortBy) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

		return mentorRepository.findByHourlyRateBetweenAndStatus(min, max, MentorStatus.APPROVED, pageable);
	}

	/* ================================================================
	 * METHOD: searchByRating
	 * DESCRIPTION:
	 * Paged search targeting only mentors exceeding the given average rating.
	 * ================================================================ */
	public Page<Mentor> searchByRating(Double rating, int page, int size, String sortBy) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

		return mentorRepository.findByAverageRatingGreaterThanEqualAndStatus(rating, MentorStatus.APPROVED, pageable);
	}

	/* ================================================================
	 * METHOD: searchMentorBySkill
	 * DESCRIPTION:
	 * Returns mappings specifically explicitly providing the given skill mapping ID.
	 * ================================================================ */
	public Page<Mentor> searchMentorBySkill(Long skillId, int page, int size) {

		List<MentorSkill> mappings = mentorSkillRepository.findBySkillId(skillId);

		List<Long> mentorIds = mappings.stream().map(MentorSkill::getMentorId).toList();

		Pageable pageable = PageRequest.of(page, size);

		return mentorRepository.findByMentorIdInAndStatus(mentorIds, MentorStatus.APPROVED, pageable);
	}

	/* ================================================================
	 * METHOD: searchMentors
	 * DESCRIPTION:
	 * Composite JPA Specification resolving combined search metadata queries.
	 * ================================================================ */
	public Page<Mentor> searchMentors(Double minPrice, Double maxPrice, Double rating, Boolean available, int page,
			int size, String sortBy) {

		Specification<Mentor> spec = Specification.where(MentorSpecification.hasMinPrice(minPrice))
				.and(MentorSpecification.hasMaxPrice(maxPrice)).and(MentorSpecification.hasRating(rating))
				.and(MentorSpecification.isAvailable(available))
				.and((root, q, cb) -> cb.equal(root.get("status"), MentorStatus.APPROVED));

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

		return mentorRepository.findAll(spec, pageable);
	}

	/* ================================================================
	 * METHOD: searchMentorsAdvanced
	 * DESCRIPTION:
	 * Deploys extended multidimensional constraints mapping capabilities contextually.
	 * ================================================================ */
	public Page<Mentor> searchMentorsAdvanced(Long skillId, Double minPrice, Double maxPrice, Double rating,
			Boolean available, int page, int size, String sortBy) {

		Specification<Mentor> spec = Specification.where(MentorSearchSpecification.hasSkill(skillId))
				.and(MentorSearchSpecification.minPrice(minPrice)).and(MentorSearchSpecification.maxPrice(maxPrice))
				.and(MentorSearchSpecification.minRating(rating)).and(MentorSearchSpecification.availability(available))
				.and((root, q, cb) -> cb.equal(root.get("status"), MentorStatus.APPROVED));

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

		return mentorRepository.findAll(spec, pageable);
	}

	/* ================================================================
	 * METHOD: updateRating
	 * DESCRIPTION:
	 * Incrementally calculates rolling average modifications internally updating models.
	 * ================================================================ */
	public void updateRating(Long mentorId, Double rating) {
		log.info("Updating rating for mentor id: {}", mentorId);

		if (rating < 0 || rating > 5) {
			log.error("Invalid rating: {} for mentor id: {}", rating, mentorId);
			throw new RuntimeException("Invalid rating");
		}

		Mentor mentor = mentorRepository.findById(mentorId).orElseThrow(() -> new RuntimeException("Mentor not found"));

		double avg = mentor.getAverageRating();
		int total = mentor.getTotalSessions();

		double newAvg = ((avg * total) + rating) / (total + 1);

		mentor.setAverageRating(newAvg);
		mentor.setTotalSessions(total + 1);

		mentorRepository.save(mentor);
	}



	/* ================================================================
	 * METHOD: mentorExists
	 * DESCRIPTION:
	 * Helper verifying whether an explicit explicit Mentor ID inherently exists.
	 * ================================================================ */
	public Boolean mentorExists(Long mentorId) {
		return mentorRepository.existsById(mentorId);
	}

    /* ================================================================
     * METHOD: getUserIdByMentorId
     * DESCRIPTION:
     * Resolves backwards to fetch the User parent entity of the Mentor wrapper.
     * ================================================================ */
    public Long getUserIdByMentorId(Long mentorId) {
        return mentorRepository.findById(mentorId)
                .map(Mentor::getUserId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));
    }
    
    /* ================================================================
     * METHOD: getMentorIdByUserId
     * DESCRIPTION:
     * Retrieves the precise mentor abstraction ID for an authenticated User ID.
     * ================================================================ */
    public Long getMentorIdByUserId(Long userId) {
        return mentorRepository.findByUserId(userId)
                .map(Mentor::getMentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found matching the userId"+userId));
    }

	/* ================================================================
	 * METHOD: map
	 * DESCRIPTION:
	 * Utility standardizing the return payload shape.
	 * ================================================================ */
	private MentorProfileResponseDto map(Mentor mentor, String msg) {

		return MentorProfileResponseDto.builder().mentorId(mentor.getMentorId()).userId(mentor.getUserId())
				.bio(mentor.getBio()).experienceYears(mentor.getExperienceYears()).hourlyRate(mentor.getHourlyRate())
				.averageRating(mentor.getAverageRating()).totalSessions(mentor.getTotalSessions())
				.available(mentor.getAvailable()).mentorSkills(mentor.getMentorSkills())
				.profileImage(mentor.getProfileImage()).upiId(mentor.getUpiId())
				.qrCodeImage(mentor.getQrCodeImage()).message(msg).build();
	}
}