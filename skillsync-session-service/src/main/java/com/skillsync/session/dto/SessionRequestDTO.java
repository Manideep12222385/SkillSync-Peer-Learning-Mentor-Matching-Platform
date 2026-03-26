package com.skillsync.session.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequestDTO {

    private LocalDateTime sessionTime;
    private Integer durationMinutes;
}