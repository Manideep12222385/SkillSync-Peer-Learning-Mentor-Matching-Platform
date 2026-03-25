package com.skillsync.auth.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
public class ForgotPasswordRequestDto {

    @Email
    private String email;
}