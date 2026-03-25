package com.skillsync.review.dto;

import lombok.Data;

@Data
public class SessionDTO {

    private Long id;
    private Long mentorId;
    private Long learnerId;
    private String status;
}