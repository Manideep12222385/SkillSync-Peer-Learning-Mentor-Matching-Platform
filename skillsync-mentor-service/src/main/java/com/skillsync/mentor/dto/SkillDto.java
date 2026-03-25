package com.skillsync.mentor.dto;

import lombok.*;

@Data
public class SkillDto {

    private Long skillId;
    private String skillName;
    private String category;
    private Boolean active;
}