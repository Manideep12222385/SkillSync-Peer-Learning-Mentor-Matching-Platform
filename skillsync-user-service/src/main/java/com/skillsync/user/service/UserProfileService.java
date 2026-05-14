//package com.skillsync.user.service;
//
//import com.skillsync.user.client.AuthClient;
//import com.skillsync.user.entity.UserProfile;
//import com.skillsync.user.repository.UserProfileRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class UserProfileService {
//
//    private final UserProfileRepository repository;
//    
//    private final AuthClient authClient;
//
//    public UserProfile createProfile(UserProfile profile) {
//
//        Boolean exists = authClient.validateUser(profile.getUserId());
//
//        if (exists == null || !exists) {
//            throw new RuntimeException("User does not exist in Auth Service");
//        }
//
//        return repository.save(profile);
//    }
//
//    public UserProfile getProfile(Long userId) {
//        return repository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User profile not found"));
//    }
//
//    public List<UserProfile> getAllProfiles() {
//        return repository.findAll();
//    }
//
//    public UserProfile updateProfile(Long userId, UserProfile updated) {
//        UserProfile existing = getProfile(userId);
//
//        existing.setFullName(updated.getFullName());
//        existing.setHeadline(updated.getHeadline());
//        existing.setBio(updated.getBio());
//        existing.setPhone(updated.getPhone());
//        existing.setTimezone(updated.getTimezone());
//
//        return repository.save(existing);
//    }
//    
//    public Boolean userExists(Long userId) {
//        return repository.existsById(userId);
//    }
//}

package com.skillsync.user.service;

import com.skillsync.user.client.AuthClient;
import com.skillsync.user.dto.*;
import com.skillsync.user.entity.UserProfile;
import com.skillsync.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: UserProfileService
 * DESCRIPTION:
 * This service handles the business logic for creating, fetching, 
 * updating, and validating user profiles internally. 
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository repository;
    private final AuthClient authClient;

    /* ================================================================
     * METHOD: createProfile
     * DESCRIPTION:
     * Validates if the user exists in AuthService, then persists a 
     * new profile bound to the user ID.
     * ================================================================ */
    public UserProfileResponse createProfile(Long userId, UserProfileRequest request) {

        log.info("Creating profile for user id: {}", userId);
        if (repository.existsById(userId)) {
            log.error("Profile already exists for user id: {}", userId);
            throw new RuntimeException("Profile already exists");
        }

//        Boolean exists = authClient.validateUser(userId);
//        if (exists == null || !exists) {
//            log.error("User does not exist in Auth Service for user id: {}", userId);
//            throw new RuntimeException("User does not exist in Auth Service");
//        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .headline(request.getHeadline())
                .bio(request.getBio())
                .phone(request.getPhone())
                .timezone(request.getTimezone())
                .profileImage(request.getProfileImage())
                .build();

        return map(repository.save(profile));
    }

    /* ================================================================
     * METHOD: getProfile
     * DESCRIPTION:
     * Fetches an existing user profile by their user ID.
     * ================================================================ */
    public UserProfileResponse getProfile(Long userId) {
        log.info("Fetching profile for user id: {}", userId);
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile not found for user id: {}", userId);
                    return new RuntimeException("Profile not found");
                });

        return map(profile);
    }

    /* ================================================================
     * METHOD: updateProfile
     * DESCRIPTION:
     * Partially updates user profile fields provided within the request.
     * ================================================================ */
    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) {
        log.info("Updating profile for user id: {}", userId);
        UserProfile existing = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Profile not found for update for user id: {}", userId);
                    return new RuntimeException("Profile not found");
                });

        if (request.getFullName() != null)
            existing.setFullName(request.getFullName());

        if (request.getHeadline() != null)
            existing.setHeadline(request.getHeadline());

        if (request.getBio() != null)
            existing.setBio(request.getBio());

        if (request.getPhone() != null)
            existing.setPhone(request.getPhone());

        if (request.getTimezone() != null)
            existing.setTimezone(request.getTimezone());

        if (request.getProfileImage() != null)
            existing.setProfileImage(request.getProfileImage());

        return map(repository.save(existing));
    }

    /* ================================================================
     * METHOD: userExists
     * DESCRIPTION:
     * Returns true if a user profile is found in the database.
     * ================================================================ */
    public Boolean userExists(Long userId) {
        return repository.existsById(userId);
    }

    /* ================================================================
     * METHOD: map
     * DESCRIPTION:
     * Helper method to map UserProfile entity to a UserProfileResponse.
     * ================================================================ */
    private UserProfileResponse map(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .headline(profile.getHeadline())
                .bio(profile.getBio())
                .phone(profile.getPhone())
                .timezone(profile.getTimezone())
                .profileImage(profile.getProfileImage())
                .build();
    }
}