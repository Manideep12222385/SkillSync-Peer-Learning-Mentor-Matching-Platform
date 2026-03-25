package com.skillsync.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skillsync.auth.entity.*;
import com.skillsync.auth.repository.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/mentor-requests")
    public List<User> getPendingMentors() {

        return userRepository.findByRoleAndAccountStatus(
                Role.ROLE_MENTOR,
                AccountStatus.PENDING
        );
    }

    @PutMapping("/mentors/{id}/approve")
    public String approveMentor(@PathVariable Long id) {

        User user = getUser(id);

        if (user.getRole() != Role.ROLE_MENTOR)
            throw new RuntimeException("Only mentor accounts can be approved");

        if (user.getAccountStatus() == AccountStatus.ACTIVE)
            throw new RuntimeException("Mentor already approved");

        if (user.getAccountStatus() == AccountStatus.REJECTED)
            throw new RuntimeException("Rejected mentor cannot be approved");

        if (user.getAccountStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Blocked mentor cannot be approved");

        if (user.getAccountStatus() != AccountStatus.PENDING)
            throw new RuntimeException("Invalid mentor state");

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEnabled(true);

        userRepository.save(user);

        return "Mentor approved successfully";
    }

    @PutMapping("/mentors/{id}/reject")
    public String rejectMentor(@PathVariable Long id) {

        User user = getUser(id);

        if (user.getRole() != Role.ROLE_MENTOR)
            throw new RuntimeException("Only mentor accounts can be rejected");

        if (user.getAccountStatus() == AccountStatus.REJECTED)
            throw new RuntimeException("Mentor already rejected");

        if (user.getAccountStatus() != AccountStatus.PENDING)
            throw new RuntimeException("Only pending mentor can be rejected");

        user.setAccountStatus(AccountStatus.REJECTED);
        user.setEnabled(false);

        userRepository.save(user);

        return "Mentor rejected successfully";
    }

    @PutMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id) {

        User user = getUser(id);

        if (user.getRole() == Role.ROLE_ADMIN)
            throw new RuntimeException("Admin account cannot be blocked");

        if (user.getAccountStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("User already blocked");

        if (user.getAccountStatus() == AccountStatus.PENDING)
            throw new RuntimeException("Pending mentor must be approved or rejected first");

        if (user.getAccountStatus() == AccountStatus.REJECTED)
            throw new RuntimeException("Rejected mentor cannot be blocked");

        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setEnabled(false);

        userRepository.save(user);

        return "User blocked successfully";
    }

    @PutMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id) {

        User user = getUser(id);

        if (user.getAccountStatus() == AccountStatus.ACTIVE)
            throw new RuntimeException("User already active");

        if (user.getAccountStatus() == AccountStatus.PENDING)
            throw new RuntimeException("Pending mentor must be approved first");

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEnabled(true);

        userRepository.save(user);

        return "User activated successfully";
    }

    @GetMapping("/users/blocked")
    public List<User> getBlockedUsers() {

        return userRepository.findByAccountStatus(AccountStatus.BLOCKED);
    }

    private User getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        User user = getUser(id);

        if (user.getRole() == Role.ROLE_ADMIN)
            throw new RuntimeException("Admin account cannot be deleted");

        if (user.getAccountStatus() == AccountStatus.ACTIVE)
            throw new RuntimeException("Active user cannot be deleted. Block first.");

        userRepository.delete(user);

        return "User deleted permanently";
    }
}