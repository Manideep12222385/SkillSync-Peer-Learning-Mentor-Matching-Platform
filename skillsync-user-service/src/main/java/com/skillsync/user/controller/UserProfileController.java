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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    // ✅ CREATE PROFILE
    @PostMapping("/profile")
    public UserProfileResponse create(
            @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.createProfile(userId, request);
    }

    // ✅ GET OWN PROFILE ONLY
    @GetMapping("/me")
    public UserProfileResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.getProfile(userId);
    }

    // ❗ INTERNAL USE ONLY (microservice call)
    @GetMapping("/exists/{id}")
    public Boolean userExists(@PathVariable Long id) {
        return service.userExists(id);
    }

    // ✅ UPDATE OWN PROFILE
    @PutMapping("/profile")
    public UserProfileResponse update(
            @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        return service.updateProfile(userId, request);
    }
}