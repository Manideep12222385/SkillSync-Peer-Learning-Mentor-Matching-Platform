package com.skillsync.skill.service;

import com.skillsync.skill.dto.CreateSkillRequestDto;
import com.skillsync.skill.dto.SkillResponseDto;
import com.skillsync.skill.dto.UpdateSkillRequestDto;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private Skill testSkill;
    private CreateSkillRequestDto createRequest;

    @BeforeEach
    void setUp() {
        testSkill = new Skill();
        testSkill.setSkillId(1L);
        testSkill.setSkillName("Java");
        testSkill.setCategory("Programming");
        testSkill.setActive(true);

        createRequest = new CreateSkillRequestDto();
        createRequest.setSkillName("Java");
        createRequest.setCategory("Programming");
    }

    // --- CREATE SKILL TESTS ---
    @Test
    void testCreateSkill_Success() {
        when(skillRepository.findBySkillName(anyString())).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        SkillResponseDto response = skillService.createSkill(createRequest);

        assertNotNull(response);
        assertEquals("Java", response.getSkillName());
        assertEquals("Skill created", response.getMessage());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    void testCreateSkill_AlreadyExists() {
        when(skillRepository.findBySkillName(anyString())).thenReturn(Optional.of(testSkill));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> skillService.createSkill(createRequest));
        assertEquals("Skill already exists", ex.getMessage());
    }

    // --- UPDATE SKILL TESTS ---
    @Test
    void testUpdateSkill_Success_AllFields() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        UpdateSkillRequestDto updateRequest = new UpdateSkillRequestDto();
        updateRequest.setSkillName("Python");
        updateRequest.setCategory("Scripting");
        updateRequest.setActive(false);
        
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        SkillResponseDto response = skillService.updateSkill(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Skill updated", response.getMessage());
        verify(skillRepository, times(1)).save(any(Skill.class));
        assertEquals("Python", testSkill.getSkillName());
        assertEquals("Scripting", testSkill.getCategory());
        assertFalse(testSkill.getActive());
    }

    @Test
    void testUpdateSkill_Success_NullFields() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        UpdateSkillRequestDto updateRequest = new UpdateSkillRequestDto();
        // keep everything null

        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        SkillResponseDto response = skillService.updateSkill(1L, updateRequest);

        assertNotNull(response);
        verify(skillRepository, times(1)).save(any(Skill.class));
        // properties should remain from testSkill setUp
        assertEquals("Java", testSkill.getSkillName());
        assertEquals("Programming", testSkill.getCategory());
        assertTrue(testSkill.getActive());
    }

    @Test
    void testUpdateSkill_NotFound() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateSkillRequestDto updateRequest = new UpdateSkillRequestDto();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> skillService.updateSkill(1L, updateRequest));
        assertEquals("Skill not found", ex.getMessage());
    }

    // --- DELETE SKILL TESTS ---
    @Test
    void testDeleteSkill_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        String response = skillService.deleteSkill(1L);

        assertEquals("Skill deleted", response);
        verify(skillRepository, times(1)).delete(any(Skill.class));
    }

    @Test
    void testDeleteSkill_NotFound() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> skillService.deleteSkill(1L));
        assertEquals("Skill not found", ex.getMessage());
    }

    // --- GET/SEARCH/EXISTS SKILL TESTS ---
    @Test
    void testGetAllActiveSkills() {
        Page<Skill> page = new PageImpl<>(Collections.singletonList(testSkill));
        when(skillRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        Page<Skill> result = skillService.getAllActiveSkills(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchSkills() {
        Page<Skill> page = new PageImpl<>(Collections.singletonList(testSkill));
        when(skillRepository.findBySkillNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        Page<Skill> result = skillService.searchSkills("Java", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSkillExists() {
        when(skillRepository.existsBySkillIdAndActiveTrue(1L)).thenReturn(true);
        assertTrue(skillService.skillExists(1L));
        
        when(skillRepository.existsBySkillIdAndActiveTrue(2L)).thenReturn(false);
        assertFalse(skillService.skillExists(2L));
    }
}
