package com.skillsync.mentor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mentorId;

    private Long userId;   // from auth service

    private String bio;

    private Integer experienceYears;

    private Double hourlyRate;

    private Double rating;

    @Enumerated(EnumType.STRING)
    private MentorStatus status;

    private LocalDateTime createdAt;
}