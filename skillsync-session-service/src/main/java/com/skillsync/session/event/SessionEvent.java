package com.skillsync.session.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEvent {

    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private String status;
    private LocalDateTime sessionTime;
}