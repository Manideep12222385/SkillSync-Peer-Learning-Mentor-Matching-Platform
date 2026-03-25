package com.skillsync.skill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class CreateSkillRequestDto {

    @NotBlank
    private String skillName;

    private String category;
}