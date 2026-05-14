package com.skillsync.session.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequestDTO {

    private Long mentorId;
    private LocalDateTime sessionTime;
    private Integer durationMinutes;
    private String topic;
}