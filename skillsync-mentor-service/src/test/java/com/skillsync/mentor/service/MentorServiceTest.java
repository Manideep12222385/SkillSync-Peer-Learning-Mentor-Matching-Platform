package com.skillsync.mentor.service;

import com.skillsync.mentor.dto.CreateMentorProfileRequestDto;
import com.skillsync.mentor.dto.MentorProfileResponseDto;
import com.skillsync.mentor.dto.SkillDto;
import com.skillsync.mentor.dto.UpdateMentorProfileRequestDto;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorSkill;
import com.skillsync.mentor.entity.MentorStatus;
import com.skillsync.mentor.feign.SkillClient;
import com.skillsync.mentor.repository.MentorRepository;
import com.skillsync.mentor.repository.MentorSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorServiceTest {

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private MentorSkillRepository mentorSkillRepository;

    @Mock
    private SkillClient skillClient;

    @InjectMocks
    private MentorService mentorService;

    private CreateMentorProfileRequestDto createRequest;
    private UpdateMentorProfileRequestDto updateRequest;
    private Mentor testMentor;
    private SkillDto testSkill;

    @BeforeEach
    void setUp() {
        createRequest = new CreateMentorProfileRequestDto();
        createRequest.setBio("Experienced developer");
        createRequest.setExperienceYears(5);
        createRequest.setHourlyRate(50.0);
        createRequest.setAvailable(true);

        updateRequest = new UpdateMentorProfileRequestDto();
        updateRequest.setBio("Updated bio");
        updateRequest.setExperienceYears(6);
        updateRequest.setHourlyRate(60.0);
        updateRequest.setAvailable(false);

        testMentor = new Mentor();
        testMentor.setMentorId(1L);
        testMentor.setUserId(10L);
        testMentor.setBio("Experienced developer");
        testMentor.setExperienceYears(5);
        testMentor.setHourlyRate(50.0);
        testMentor.setAverageRating(4.5);
        testMentor.setTotalSessions(10);
        testMentor.setAvailable(true);
        testMentor.setStatus(MentorStatus.APPROVED);

        testSkill = new SkillDto();
        testSkill.setSkillId(100L);
        testSkill.setSkillName("Java");
        testSkill.setActive(true);
    }

    // --- CREATE PROFILE TESTS ---
    @Test
    void testCreateProfile_Success() {
        when(mentorRepository.existsByUserId(10L)).thenReturn(false);
        when(mentorRepository.save(any(Mentor.class))).thenReturn(testMentor);

        MentorProfileResponseDto response = mentorService.createProfile(10L, createRequest);

        assertNotNull(response);
        assertEquals("Profile created successfully.", response.getMessage());
        verify(mentorRepository, times(1)).save(any(Mentor.class));
    }

    @Test
    void testCreateProfile_AlreadyExists() {
        when(mentorRepository.existsByUserId(10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.createProfile(10L, createRequest));
        assertEquals("Mentor profile already exists", ex.getMessage());
    }

    @Test
    void testCreateProfile_NegativeHourlyRate() {
        when(mentorRepository.existsByUserId(10L)).thenReturn(false);
        createRequest.setHourlyRate(-10.0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.createProfile(10L, createRequest));
        assertEquals("Hourly rate cannot be negative", ex.getMessage());
    }

    @Test
    void testCreateProfile_NegativeExperience() {
        when(mentorRepository.existsByUserId(10L)).thenReturn(false);
        createRequest.setExperienceYears(-1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.createProfile(10L, createRequest));
        assertEquals("Experience cannot be negative", ex.getMessage());
    }

    // --- UPDATE PROFILE TESTS ---
    @Test
    void testUpdateProfile_Success() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(testMentor);

        MentorProfileResponseDto response = mentorService.updateProfile(10L, updateRequest);

        assertNotNull(response);
        assertEquals("Profile updated", response.getMessage());
        assertEquals("Updated bio", testMentor.getBio());
        assertEquals(6, testMentor.getExperienceYears());
        verify(mentorRepository, times(1)).save(any(Mentor.class));
    }

    @Test
    void testUpdateProfile_NotFound() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.updateProfile(10L, updateRequest));
        assertEquals("Mentor profile not found", ex.getMessage());
    }

    @Test
    void testUpdateProfile_NotApproved() {
        testMentor.setStatus(MentorStatus.PENDING);
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.updateProfile(10L, updateRequest));
        assertEquals("Mentor not approved yet", ex.getMessage());
    }

    // --- ADD SKILL TESTS ---
    @Test
    void testAddSkillToMentor_Success() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));
        when(skillClient.getSkillById(100L)).thenReturn(testSkill);
        when(mentorSkillRepository.findByMentorId(1L)).thenReturn(Collections.emptyList());

        String response = mentorService.addSkillToMentor(10L, 100L);

        assertEquals("Skill added", response);
        verify(mentorSkillRepository, times(1)).save(any(MentorSkill.class));
    }

    @Test
    void testAddSkillToMentor_InactiveSkill() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));
        testSkill.setActive(false);
        when(skillClient.getSkillById(100L)).thenReturn(testSkill);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.addSkillToMentor(10L, 100L));
        assertEquals("Skill is inactive", ex.getMessage());
    }

    @Test
    void testAddSkillToMentor_AlreadyExists() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));
        when(skillClient.getSkillById(100L)).thenReturn(testSkill);
        
        MentorSkill existingSkill = MentorSkill.builder().mentorId(1L).skillId(100L).build();
        when(mentorSkillRepository.findByMentorId(1L)).thenReturn(Collections.singletonList(existingSkill));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.addSkillToMentor(10L, 100L));
        assertEquals("Skill already added", ex.getMessage());
    }

    // --- SEARCH TESTS ---
    @Test
    void testSearchByPrice() {
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(testMentor));
        when(mentorRepository.findByHourlyRateBetweenAndStatus(eq(10.0), eq(100.0), eq(MentorStatus.APPROVED), any(Pageable.class)))
                .thenReturn(page);

        Page<Mentor> result = mentorService.searchByPrice(10.0, 100.0, 0, 10, "hourlyRate");
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchByRating() {
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(testMentor));
        when(mentorRepository.findByAverageRatingGreaterThanEqualAndStatus(eq(4.0), eq(MentorStatus.APPROVED), any(Pageable.class)))
                .thenReturn(page);

        Page<Mentor> result = mentorService.searchByRating(4.0, 0, 10, "averageRating");
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchMentorBySkill() {
        MentorSkill mapping = MentorSkill.builder().mentorId(1L).skillId(100L).build();
        when(mentorSkillRepository.findBySkillId(100L)).thenReturn(Collections.singletonList(mapping));
        
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(testMentor));
        when(mentorRepository.findByMentorIdInAndStatus(any(), eq(MentorStatus.APPROVED), any(Pageable.class)))
                .thenReturn(page);

        Page<Mentor> result = mentorService.searchMentorBySkill(100L, 0, 10);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearchMentors() {
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(testMentor));
        when(mentorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Mentor> result = mentorService.searchMentors(10.0, 100.0, 4.0, true, 0, 10, "hourlyRate");
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearchMentorsAdvanced() {
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(testMentor));
        when(mentorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Mentor> result = mentorService.searchMentorsAdvanced(100L, 10.0, 100.0, 4.0, true, 0, 10, "hourlyRate");
        assertEquals(1, result.getTotalElements());
    }

    // --- UPDATE RATING TESTS ---
    @Test
    void testUpdateRating_Success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(testMentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(testMentor);

        mentorService.updateRating(1L, 5.0);

        verify(mentorRepository, times(1)).save(any(Mentor.class));
        // new avg = ((4.5 * 10) + 5.0) / 11 = 50 / 11 = 4.545
        assertEquals(11, testMentor.getTotalSessions());
    }

    @Test
    void testUpdateRating_InvalidRatingLow() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.updateRating(1L, -1.0));
        assertEquals("Invalid rating", ex.getMessage());
    }

    @Test
    void testUpdateRating_InvalidRatingHigh() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.updateRating(1L, 6.0));
        assertEquals("Invalid rating", ex.getMessage());
    }

    @Test
    void testUpdateRating_MentorNotFound() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.updateRating(1L, 5.0));
        assertEquals("Mentor not found", ex.getMessage());
    }

    // --- UTILITY METHODS TESTS ---
    @Test
    void testMentorExists() {
        when(mentorRepository.existsById(1L)).thenReturn(true);
        assertTrue(mentorService.mentorExists(1L));
    }

    @Test
    void testGetUserIdByMentorId_Success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(testMentor));
        assertEquals(10L, mentorService.getUserIdByMentorId(1L));
    }

    @Test
    void testGetUserIdByMentorId_NotFound() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.getUserIdByMentorId(1L));
        assertEquals("Mentor not found", ex.getMessage());
    }

    @Test
    void testGetMentorIdByUserId_Success() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.of(testMentor));
        assertEquals(1L, mentorService.getMentorIdByUserId(10L));
    }

    @Test
    void testGetMentorIdByUserId_NotFound() {
        when(mentorRepository.findByUserId(10L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> mentorService.getMentorIdByUserId(10L));
        assertEquals("Mentor not found matching the userId10", ex.getMessage());
    }
}
