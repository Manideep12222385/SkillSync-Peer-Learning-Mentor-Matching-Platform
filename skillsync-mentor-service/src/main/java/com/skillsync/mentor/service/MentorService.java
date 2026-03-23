package com.skillsync.mentor.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skillsync.mentor.dto.ApplyMentorRequestDto;
import com.skillsync.mentor.dto.MentorResponseDto;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorStatus;
import com.skillsync.mentor.repository.MentorRepository;

@Service
public class MentorService {

    @Autowired
    private MentorRepository mentorRepository;

    public MentorResponseDto applyMentor(ApplyMentorRequestDto request) {

        mentorRepository.findByUserId(request.getUserId())
                .ifPresent(m -> {
                    throw new RuntimeException("User already applied as mentor");
                });

        Mentor mentor = Mentor.builder()
                .userId(request.getUserId())
                .bio(request.getBio())
                .experienceYears(request.getExperienceYears())
                .hourlyRate(request.getHourlyRate())
                .rating(0.0)
                .status(MentorStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        mentorRepository.save(mentor);

        return MentorResponseDto.builder()
                .mentorId(mentor.getMentorId())
                .userId(mentor.getUserId())
                .bio(mentor.getBio())
                .experienceYears(mentor.getExperienceYears())
                .hourlyRate(mentor.getHourlyRate())
                .rating(mentor.getRating())
                .status(mentor.getStatus().name())
                .message("Mentor application submitted")
                .build();
    }
}