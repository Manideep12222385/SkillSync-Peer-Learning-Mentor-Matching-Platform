package com.skillsync.auth.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillsync.auth.dto.*;
import com.skillsync.auth.entity.*;
import com.skillsync.auth.repository.PasswordResetOtpRepository;
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

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    public RegisterResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already taken");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already registered");

        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");

        Role role;
        try {
            role = Role.valueOf(request.getRole());
        } catch (Exception e) {
            throw new RuntimeException("Invalid role selected");
        }

        if (role == Role.ROLE_ADMIN)
            throw new RuntimeException("Admin registration is not allowed");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        if (role == Role.ROLE_MENTOR) {
            user.setEnabled(false);
            user.setAccountStatus(AccountStatus.PENDING);
        } else {
            user.setEnabled(true);
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        return RegisterResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid email or password");

        if (user.getAccountStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Account not active. Please wait for admin approval.");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());

        return LoginResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .message("Login successful")
                .build();
    }
    @Transactional
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        PasswordResetOtp entity = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.deleteByEmail(email);
        otpRepository.save(entity);

        emailService.sendOtp(email, otp);

        return "OTP sent to email";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequestDto request) {

        PasswordResetOtp otpEntity = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("OTP not requested"));

        if (!otpEntity.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP");

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
            throw new RuntimeException("New password cannot be same as old password");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.deleteByEmail(request.getEmail());

        return "Password reset successful";
    }
}