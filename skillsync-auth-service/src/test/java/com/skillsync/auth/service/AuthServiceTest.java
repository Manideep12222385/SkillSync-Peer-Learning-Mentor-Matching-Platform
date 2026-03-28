package com.skillsync.auth.service;

import com.skillsync.auth.dto.LoginRequestDto;
import com.skillsync.auth.dto.LoginResponseDto;
import com.skillsync.auth.dto.RegisterRequestDto;
import com.skillsync.auth.dto.RegisterResponseDto;
import com.skillsync.auth.dto.ResetPasswordRequestDto;
import com.skillsync.auth.entity.AccountStatus;
import com.skillsync.auth.entity.PasswordResetOtp;
import com.skillsync.auth.entity.Role;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.repository.PasswordResetOtpRepository;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setRole("ROLE_LEARNER");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_LEARNER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
    }

    // --- REGISTRATION TESTS ---
    @Test
    void testRegister_Success_Learner() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // We capture the saved user implicitly via the mock
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setUserId(1L);
            saved.setUsername(registerRequest.getUsername());
            saved.setEmail(registerRequest.getEmail());
            saved.setRole(Role.ROLE_LEARNER);
            return saved;
        });

        RegisterResponseDto response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("ROLE_LEARNER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_Success_Mentor() {
        registerRequest.setRole("ROLE_MENTOR");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setUserId(2L);
            saved.setUsername(registerRequest.getUsername());
            saved.setEmail(registerRequest.getEmail());
            saved.setRole(Role.ROLE_MENTOR);
            // Internal logic sets PENDING and disabled
            assertFalse(saved.getEnabled());
            assertEquals(AccountStatus.PENDING, saved.getAccountStatus());
            return saved;
        });

        RegisterResponseDto response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("ROLE_MENTOR", response.getRole());
    }

    @Test
    void testRegister_InvalidRole() {
        registerRequest.setRole("INVALID_ROLE");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Invalid role selected", ex.getMessage());
    }

    @Test
    void testRegister_RoleAdminNotAllowed() {
        registerRequest.setRole("ROLE_ADMIN");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Admin registration is not allowed", ex.getMessage());
    }

    @Test
    void testRegister_UsernameTaken() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testRegister_EmailTaken() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testRegister_PasswordsDoNotMatch() {
        registerRequest.setConfirmPassword("differentPassword");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    // --- LOGIN TESTS ---
    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong())).thenReturn("mockedToken");

        LoginResponseDto response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockedToken", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testLogin_InvalidEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_AccountNotActive() {
        testUser.setAccountStatus(AccountStatus.PENDING);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    // --- FORGOT PASSWORD TESTS ---
    @Test
    void testForgotPassword_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        String response = authService.forgotPassword("test@example.com");

        assertEquals("OTP sent to email", response);
        verify(otpRepository, times(1)).deleteByEmail("test@example.com");
        verify(otpRepository, times(1)).save(any(PasswordResetOtp.class));
        verify(emailService, times(1)).sendOtp(eq("test@example.com"), anyString());
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.forgotPassword("test@example.com"));
        assertEquals("User not found", ex.getMessage());
    }

    // --- RESET PASSWORD TESTS ---
    @Test
    void testResetPassword_Success() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        resetRequest.setOtp("123456");
        resetRequest.setNewPassword("newPassword123");

        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("newPassword123", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        String response = authService.resetPassword(resetRequest);

        assertEquals("Password reset successful", response);
        verify(userRepository, times(1)).save(testUser);
        verify(otpRepository, times(1)).deleteByEmail("test@example.com");
    }

    @Test
    void testResetPassword_OtpNotRequested() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        
        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
        assertEquals("OTP not requested", ex.getMessage());
    }

    @Test
    void testResetPassword_InvalidOtp() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        resetRequest.setOtp("wrongOtp");

        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validOtp));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
        assertEquals("Invalid OTP", ex.getMessage());
    }

    @Test
    void testResetPassword_OtpExpired() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        resetRequest.setOtp("123456");

        PasswordResetOtp expiredOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().minusMinutes(1)) // expired
                .build();

        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.of(expiredOtp));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
        assertEquals("OTP expired", ex.getMessage());
    }

    @Test
    void testResetPassword_UserNotFound() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        resetRequest.setOtp("123456");
        resetRequest.setNewPassword("newPassword123");

        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testResetPassword_NewPasswordSameAsOld() {
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto();
        resetRequest.setEmail("test@example.com");
        resetRequest.setOtp("123456");
        resetRequest.setNewPassword("oldPassword");

        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
        assertEquals("New password cannot be same as old password", ex.getMessage());
    }
}
