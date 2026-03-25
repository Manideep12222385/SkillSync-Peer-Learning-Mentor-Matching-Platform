package com.skillsync.skill.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class SkillResponseDto {

    private Long skillId;
    private String skillName;
    private String category;
    private Boolean active;
    private String message;
}