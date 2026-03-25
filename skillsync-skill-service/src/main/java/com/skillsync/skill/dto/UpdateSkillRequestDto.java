package com.skillsync.skill.dto;

import lombok.*;

@Data
public class UpdateSkillRequestDto {

    private String skillName;
    private String category;
    private Boolean active;
}