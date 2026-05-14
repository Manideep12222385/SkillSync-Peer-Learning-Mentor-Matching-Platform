package com.skillsync.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.skillsync.auth.entity.User;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.security.CustomUserDetails;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: CustomUserDetailsService
 * DESCRIPTION:
 * Integrates directly with Spring Security fetching users mapped intrinsically
 * via pure JPA converting them to contextually readable formats natively.
 * ================================================================
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /* ================================================================
     * METHOD: loadUserByUsername
     * DESCRIPTION:
     * Core resolution logic bridging Authentication Manager resolving emails natively.
     * ================================================================ */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(user);
    }
}