package com.skillsync.mentor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(length = 1000)
    private String bio;

    private Integer experienceYears;

    private Double hourlyRate;

    private Double averageRating;

    private Integer totalSessions;

    private Boolean available;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorStatus status;

    @OneToMany
    @JoinColumn(name = "mentorId",
            referencedColumnName = "mentorId",
            insertable = false,
            updatable = false)
    private List<MentorSkill> mentorSkills;
}