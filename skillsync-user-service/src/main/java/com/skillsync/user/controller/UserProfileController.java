package com.skillsync.user.controller;

import com.skillsync.user.entity.UserProfile;
import com.skillsync.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @PostMapping
    public UserProfile create(@RequestBody UserProfile profile) {
        return service.createProfile(profile);
    }

    @GetMapping("/{id}")
    public UserProfile get(@PathVariable Long id) {
        return service.getProfile(id);
    }

    @GetMapping
    public List<UserProfile> getAll() {
        return service.getAllProfiles();
    }

    @PutMapping("/{id}")
    public UserProfile update(@PathVariable Long id,
                              @RequestBody UserProfile profile) {
        return service.updateProfile(id, profile);
    }
    
    @GetMapping("/exists/{id}")
    public Boolean userExists(@PathVariable Long id) {
        return service.userExists(id);
    }
}