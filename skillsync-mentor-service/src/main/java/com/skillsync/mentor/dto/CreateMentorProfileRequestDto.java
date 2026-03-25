package com.skillsync.mentor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class CreateMentorProfileRequestDto {

    @NotNull
    private Long userId;

    @NotBlank
    private String bio;

    @NotNull
    private Integer experienceYears;

    @NotNull
    private Double hourlyRate;

    @NotNull
    private Boolean available;
}