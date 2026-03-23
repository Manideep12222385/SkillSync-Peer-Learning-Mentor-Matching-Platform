package com.skillsync.mentor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skillsync.mentor.dto.ApplyMentorRequestDto;
import com.skillsync.mentor.dto.MentorResponseDto;
import com.skillsync.mentor.service.MentorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mentors")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @PostMapping("/apply")
    public MentorResponseDto applyMentor(
            @Valid @RequestBody ApplyMentorRequestDto request) {

        return mentorService.applyMentor(request);
    }
}