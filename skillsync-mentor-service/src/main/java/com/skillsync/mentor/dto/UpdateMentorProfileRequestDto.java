package com.skillsync.mentor.dto;

import lombok.*;

@Data
public class UpdateMentorProfileRequestDto {

    private String bio;
    private Integer experienceYears;
    private Double hourlyRate;
    private Boolean available;
}