package com.skillsync.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.skillsync.auth.dto.LoginResponseDto;
import com.skillsync.auth.dto.LoginRequestDto;
import com.skillsync.auth.dto.RegisterRequestDto;
import com.skillsync.auth.dto.RegisterResponseDto;
import com.skillsync.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

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

    @GetMapping("/test")
    public String test() {
        return "JWT Security Working";
    }

    @GetMapping("/admin/test")
    public String adminTest() {
        return "Admin API";
    }

    @GetMapping("/learner/test")
    public String learnerTest() {
        return "Learner API";
    }
}