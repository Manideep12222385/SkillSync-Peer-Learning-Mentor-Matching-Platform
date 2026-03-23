package com.skillsync.mentor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyMentorRequestDto {

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    @NotNull(message = "Hourly rate is required")
    @Min(value = 1, message = "Hourly rate must be positive")
    private Double hourlyRate;
}