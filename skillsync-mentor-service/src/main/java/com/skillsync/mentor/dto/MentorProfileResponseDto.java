package com.skillsync.mentor.dto;

import lombok.*;
import com.skillsync.mentor.entity.MentorSkill;

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
    private java.util.List<MentorSkill> mentorSkills;
    private String profileImage;
    private String upiId;
    private String qrCodeImage;
    private String message;
}