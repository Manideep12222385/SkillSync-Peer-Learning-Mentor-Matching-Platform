package com.skillsync.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skillsync.auth.entity.*;
import com.skillsync.auth.repository.UserRepository;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: AdminController
 * DESCRIPTION:
 * This controller handles administrative endpoints like managing and 
 * verifying mentor registration approvals, blocking, and fetching lists.
 * ================================================================
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    /* ================================================================
     * METHOD: getPendingMentors
     * DESCRIPTION:
     * Returns a list of mentors that have registered but are awaiting approval.
     * ================================================================ */
    @GetMapping("/mentor-requests")
    public List<User> getPendingMentors() {

        return userRepository.findByRoleAndAccountStatus(
                Role.ROLE_MENTOR,
                AccountStatus.PENDING
        );
    }

    /* ================================================================
     * METHOD: approveMentor
     * DESCRIPTION:
     * Modifies the pending mentor's account status to active and verified.
     * ================================================================ */
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

    /* ================================================================
     * METHOD: rejectMentor
     * DESCRIPTION:
     * Changes a pending mentor request to rejected and disables the account.
     * ================================================================ */
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

    /* ================================================================
     * METHOD: blockUser
     * DESCRIPTION:
     * Bans any regular user or mentor from the system. Admin user overrides apply.
     * ================================================================ */
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

    /* ================================================================
     * METHOD: activateUser
     * DESCRIPTION:
     * Reactivates previously blocked users mapping them back to the platform.
     * ================================================================ */
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

    /* ================================================================
     * METHOD: getBlockedUsers
     * DESCRIPTION:
     * Facilitates tracking all user accounts actively blocked globally.
     * ================================================================ */
    @GetMapping("/users/blocked")
    public List<User> getBlockedUsers() {

        return userRepository.findByAccountStatus(AccountStatus.BLOCKED);
    }

    /* ================================================================
     * METHOD: getRejectedUsers
     * DESCRIPTION:
     * Retrieves users who have been rejected (e.g. denied mentor requests).
     * ================================================================ */
    @GetMapping("/users/rejected")
    public List<User> getRejectedUsers() {
        return userRepository.findByAccountStatus(AccountStatus.REJECTED);
    }

    private User getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping("/users/active-learners")
    public List<User> getActiveLearners() {
        return userRepository.findByRoleAndAccountStatus(Role.ROLE_LEARNER, AccountStatus.ACTIVE);
    }

    @GetMapping("/users/active-mentors")
    public List<User> getActiveMentors() {
        return userRepository.findByRoleAndAccountStatus(Role.ROLE_MENTOR, AccountStatus.ACTIVE);
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

    /* ================================================================
     * METHOD: getAdminStats
     * DESCRIPTION:
     * Retrieves high-level analytics for the admin dashboard.
     * ================================================================ */
    @GetMapping("/stats")
    public java.util.Map<String, Long> getAdminStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeLearners", (long) userRepository.findByRoleAndAccountStatus(Role.ROLE_LEARNER, AccountStatus.ACTIVE).size());
        stats.put("activeMentors", (long) userRepository.findByRoleAndAccountStatus(Role.ROLE_MENTOR, AccountStatus.ACTIVE).size());
        stats.put("pendingMentors", (long) userRepository.findByRoleAndAccountStatus(Role.ROLE_MENTOR, AccountStatus.PENDING).size());
        stats.put("blockedUsers", (long) userRepository.findByAccountStatus(AccountStatus.BLOCKED).size());
        return stats;
    }
}