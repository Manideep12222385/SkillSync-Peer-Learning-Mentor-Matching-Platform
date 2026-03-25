package com.skillsync.mentor.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class MentorProfileResponseDto {

    private Long mentorId;
    private Long userId;
    private String bio;
    private Integer experienceYears;
    private Double hourlyRate;
    private Double averageRating;
    private Integer totalSessions;
    private Boolean available;
    private String message;
}