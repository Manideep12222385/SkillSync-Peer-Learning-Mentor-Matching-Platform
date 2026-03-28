package com.skillsync.user.service;

import com.skillsync.user.client.AuthClient;
import com.skillsync.user.dto.UserProfileRequest;
import com.skillsync.user.dto.UserProfileResponse;
import com.skillsync.user.entity.UserProfile;
import com.skillsync.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserProfile testProfile;
    private UserProfileRequest testRequest;

    @BeforeEach
    void setUp() {
        testProfile = UserProfile.builder()
                .userId(1L)
                .fullName("Test User")
                .headline("Software Engineer")
                .bio("I write code")
                .phone("1234567890")
                .timezone("UTC")
                .build();

        testRequest = new UserProfileRequest();
        testRequest.setFullName("Test User");
        testRequest.setHeadline("Software Engineer");
        testRequest.setBio("I write code");
        testRequest.setPhone("1234567890");
        testRequest.setTimezone("UTC");
    }

    // --- CREATE PROFILE TESTS ---
    @Test
    void testCreateProfile_Success() {
        when(userProfileRepository.existsById(1L)).thenReturn(false);
        when(authClient.validateUser(1L)).thenReturn(true);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        UserProfileResponse response = userProfileService.createProfile(1L, testRequest);

        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testCreateProfile_AlreadyExists() {
        when(userProfileRepository.existsById(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userProfileService.createProfile(1L, testRequest));
        assertEquals("Profile already exists", ex.getMessage());
    }

    @Test
    void testCreateProfile_UserDoesNotExistInAuthService() {
        when(userProfileRepository.existsById(1L)).thenReturn(false);
        when(authClient.validateUser(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userProfileService.createProfile(1L, testRequest));
        assertEquals("User does not exist in Auth Service", ex.getMessage());
    }
    
    @Test
    void testCreateProfile_AuthClientReturnsNull() {
        when(userProfileRepository.existsById(1L)).thenReturn(false);
        when(authClient.validateUser(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userProfileService.createProfile(1L, testRequest));
        assertEquals("User does not exist in Auth Service", ex.getMessage());
    }

    // --- GET PROFILE TESTS ---
    @Test
    void testGetProfile_Success() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        UserProfileResponse response = userProfileService.getProfile(1L);

        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
    }
    
    @Test
    void testGetProfile_NotFound() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userProfileService.getProfile(1L));
        assertEquals("Profile not found", ex.getMessage());
    }

    // --- UPDATE PROFILE TESTS ---
    @Test
    void testUpdateProfile_Success_AllFields() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        UserProfileRequest updateRequest = new UserProfileRequest();
        updateRequest.setFullName("Updated User");
        updateRequest.setHeadline("Senior Engineer");
        updateRequest.setBio("I write more code");
        updateRequest.setPhone("0987654321");
        updateRequest.setTimezone("PST");

        UserProfileResponse response = userProfileService.updateProfile(1L, updateRequest);

        assertNotNull(response);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        assertEquals("Updated User", testProfile.getFullName());
        assertEquals("Senior Engineer", testProfile.getHeadline());
        assertEquals("I write more code", testProfile.getBio());
        assertEquals("0987654321", testProfile.getPhone());
        assertEquals("PST", testProfile.getTimezone());
    }
    
    @Test
    void testUpdateProfile_Success_SomeFieldsNull() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        UserProfileRequest updateRequest = new UserProfileRequest();
        updateRequest.setFullName("Updated User Only");
        // others remain null

        UserProfileResponse response = userProfileService.updateProfile(1L, updateRequest);

        assertNotNull(response);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        assertEquals("Updated User Only", testProfile.getFullName());
        assertEquals("Software Engineer", testProfile.getHeadline()); // from setup, untouched
    }
    
    @Test
    void testUpdateProfile_NotFound() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

        UserProfileRequest updateRequest = new UserProfileRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userProfileService.updateProfile(1L, updateRequest));
        assertEquals("Profile not found", ex.getMessage());
    }

    // --- EXISTS TESTS ---
    @Test
    void testUserExists() {
        when(userProfileRepository.existsById(1L)).thenReturn(true);
        assertTrue(userProfileService.userExists(1L));
        
        when(userProfileRepository.existsById(2L)).thenReturn(false);
        assertFalse(userProfileService.userExists(2L));
    }
}
