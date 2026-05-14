package com.skillsync.session.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mentorId;

    private Long learnerId;

    private LocalDateTime sessionTime;

    private Integer durationMinutes;

    private String topic;
    
    @Column(length = 500)
    private String meetingLink;
    
    @Column(length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;
}