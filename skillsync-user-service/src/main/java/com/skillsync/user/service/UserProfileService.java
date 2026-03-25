package com.skillsync.user.service;

import com.skillsync.user.client.AuthClient;
import com.skillsync.user.entity.UserProfile;
import com.skillsync.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;
    
    private final AuthClient authClient;

    public UserProfile createProfile(UserProfile profile) {

        Boolean exists = authClient.validateUser(profile.getUserId());

        if (exists == null || !exists) {
            throw new RuntimeException("User does not exist in Auth Service");
        }

        return repository.save(profile);
    }

    public UserProfile getProfile(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    public List<UserProfile> getAllProfiles() {
        return repository.findAll();
    }

    public UserProfile updateProfile(Long userId, UserProfile updated) {
        UserProfile existing = getProfile(userId);

        existing.setFullName(updated.getFullName());
        existing.setHeadline(updated.getHeadline());
        existing.setBio(updated.getBio());
        existing.setPhone(updated.getPhone());
        existing.setTimezone(updated.getTimezone());

        return repository.save(existing);
    }
    
    public Boolean userExists(Long userId) {
        return repository.existsById(userId);
    }
}