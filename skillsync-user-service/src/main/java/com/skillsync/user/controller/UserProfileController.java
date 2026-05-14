//package com.skillsync.user.controller;
//
//import com.skillsync.user.entity.UserProfile;
//import com.skillsync.user.service.UserProfileService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//@RequiredArgsConstructor
//public class UserProfileController {
//
//    private final UserProfileService service;
//
//    @PostMapping("/profile")
//    public UserProfile create(
//            @RequestBody UserProfile profile,
//            @AuthenticationPrincipal Jwt jwt) {
//            
//        Long userId = ((Number) jwt.getClaim("userId")).longValue();
//        profile.setUserId(userId);
//        return service.createProfile(profile);
//    }
//
//    @GetMapping("/{id}")
//    public UserProfile get(@PathVariable Long id) {
//        return service.getProfile(id);
//    }
//
//    @GetMapping
//    public List<UserProfile> getAll() {
//        return service.getAllProfiles();
//    }
//
//    @PutMapping("/profile")
//    public UserProfile update(
//            @RequestBody UserProfile profile,
//            @AuthenticationPrincipal Jwt jwt) {
//            
//        Long userId = ((Number) jwt.getClaim("userId")).longValue();
//        return service.updateProfile(userId, profile);
//    }
//    
//    @GetMapping("/exists/{id}")
//    public Boolean userExists(@PathVariable Long id) {
//        return service.userExists(id);
//    }
//}

package com.skillsync.user.controller;

import com.skillsync.user.dto.*;
import com.skillsync.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: UserProfileController
 * DESCRIPTION:
 * This controller handles user profile operations such as creating,
 * fetching, checking existence, and updating user profiles.
 * ================================================================
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    // ✅ CREATE PROFILE
    /* ================================================================
     * METHOD: create
     * DESCRIPTION:
     * Creates a new user profile using the provided request data 
     * and the authenticated user's ID.
     * ================================================================ */
    @PostMapping("/profile")
    public UserProfileResponse create(
            @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.createProfile(userId, request);
    }

    // ✅ GET OWN PROFILE ONLY
    /* ================================================================
     * METHOD: getMyProfile
     * DESCRIPTION:
     * Retrieves the profile of the currently authenticated user.
     * ================================================================ */
    @GetMapping("/me")
    public UserProfileResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.getProfile(userId);
    }

    // ❗ INTERNAL USE ONLY (microservice call)
    /* ================================================================
     * METHOD: userExists
     * DESCRIPTION:
     * Checks if a user profile exists for the given user ID. Internally
     * used for cross-microservice validation.
     * ================================================================ */
    @GetMapping("/exists/{id}")
    public Boolean userExists(@PathVariable Long id) {
        return service.userExists(id);
    }

    // ✅ UPDATE OWN PROFILE
    /* ================================================================
     * METHOD: update
     * DESCRIPTION:
     * Updates the currently authenticated user's profile with the 
     * newly provided profile data.
     * ================================================================ */
    @PutMapping("/profile")
    public UserProfileResponse update(
            @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.updateProfile(userId, request);
    }
}