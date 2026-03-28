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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Username already taken: {}", request.getUsername());
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already registered: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.error("Passwords do not match for user: {}", request.getEmail());
            throw new RuntimeException("Passwords do not match");
        }

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
        log.info("Attempting to login user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Invalid email provided for login: {}", request.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid password provided for login: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            log.error("Account not active for user: {}", request.getEmail());
            throw new RuntimeException("Account not active. Please wait for admin approval.");
        }

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
        log.info("Attempting to reset password for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for password reset: {}", email);
                    return new RuntimeException("User not found");
                });

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
        log.info("Attempting to verify OTP and reset password for email: {}", request.getEmail());

        PasswordResetOtp otpEntity = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("OTP not requested for email: {}", request.getEmail());
                    return new RuntimeException("OTP not requested");
                });

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            log.error("Invalid OTP for email: {}", request.getEmail());
            throw new RuntimeException("Invalid OTP");
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.error("OTP expired for email: {}", request.getEmail());
            throw new RuntimeException("OTP expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found during password reset: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.error("New password cannot be same as old password for email: {}", request.getEmail());
            throw new RuntimeException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.deleteByEmail(request.getEmail());

        return "Password reset successful";
    }
}