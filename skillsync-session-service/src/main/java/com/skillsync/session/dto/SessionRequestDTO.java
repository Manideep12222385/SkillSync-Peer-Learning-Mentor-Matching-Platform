package com.skillsync.session.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequestDTO {

    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionTime;
    private Integer durationMinutes;
}