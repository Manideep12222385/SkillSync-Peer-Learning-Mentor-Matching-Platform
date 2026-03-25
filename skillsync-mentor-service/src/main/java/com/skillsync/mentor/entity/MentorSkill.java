package com.skillsync.mentor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "mentor_skills",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"mentorId", "skillId"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mentorId;

    private Long skillId;
}