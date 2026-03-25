package com.skillsync.session.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionResponse {

    private Long id;
    private Long mentorId;
    private Long learnerId;
    private String status;
}