package com.skillsync.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private Long userId;   // same id from auth-service

    private String fullName;

    private String headline;

    private String bio;

    private String phone;

    private String timezone;
}