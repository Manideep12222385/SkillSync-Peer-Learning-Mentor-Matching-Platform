package com.skillsync.mentor.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorResponseDto {

    private Long mentorId;
    private Long userId;
    private String bio;
    private Integer experienceYears;
    private Double hourlyRate;
    private Double rating;
    private String status;
    private String message;
}