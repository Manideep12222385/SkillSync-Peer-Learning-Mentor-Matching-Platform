package com.skillsync.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillsync.auth.dto.LoginResponseDto;
import com.skillsync.auth.dto.LoginRequestDto;
import com.skillsync.auth.dto.RegisterRequestDto;
import com.skillsync.auth.dto.RegisterResponseDto;
import com.skillsync.auth.entity.Role;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public RegisterResponseDto register(RegisterRequestDto request) {
    	
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_LEARNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return RegisterResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .message("Login successful")
                .build();
    }
}