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

/**
 * @author manideep
 * 
 * Provides business logic and REST endpoints for the service.
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;   // ⭐ IMPORTANT ADD

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @PostMapping("/register")
    public RegisterResponseDto register(
            @Valid @RequestBody RegisterRequestDto request) {

        return authService.register(request);
    }

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginRequestDto request) {

        return authService.login(request);
    }

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @GetMapping("/internal/users/{userId}")
    public Boolean userExists(@PathVariable Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @GetMapping("/internal/users/{userId}/email")
    public String getUserEmail(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(com.skillsync.auth.entity.User::getEmail)
                .orElse("unknown@example.com");
    }

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @GetMapping("/internal/users/{userId}/name")
    public String getUserName(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(com.skillsync.auth.entity.User::getUsername)
                .orElse("User");
    }

    
    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequestDto request) {

        return authService.forgotPassword(request.getEmail());
    }

    /**
     * Executes the corresponding operation for this method.
     * 
     * @return adequate response or processes the request
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestBody ResetPasswordRequestDto request) {

        return authService.resetPassword(request);
    }
}