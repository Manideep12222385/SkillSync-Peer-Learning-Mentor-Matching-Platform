package com.skillsync.auth.dto;

import lombok.*;

@Data
public class ResetPasswordRequestDto {

    private String email;
    private String otp;
    private String newPassword;
}