package com.skillsync.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.skillsync.auth.dto.LoginResponseDto;
import com.skillsync.auth.dto.ForgotPasswordRequestDto;
import com.skillsync.auth.dto.LoginRequestDto;
import com.skillsync.auth.dto.RegisterRequestDto;
import com.skillsync.auth.dto.RegisterResponseDto;
import com.skillsync.auth.dto.ResetPasswordRequestDto;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;   // ⭐ IMPORTANT ADD

    @PostMapping("/register")
    public RegisterResponseDto register(
            @Valid @RequestBody RegisterRequestDto request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginRequestDto request) {

        return authService.login(request);
    }

    @GetMapping("/internal/users/{userId}")
    public Boolean userExists(@PathVariable Long userId) {
        return userRepository.existsById(userId);
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequestDto request) {

        return authService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestBody ResetPasswordRequestDto request) {

        return authService.resetPassword(request);
    }
}