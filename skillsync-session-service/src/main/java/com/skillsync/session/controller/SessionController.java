package com.skillsync.session.controller;

import com.skillsync.session.dto.SessionRequestDTO;
import com.skillsync.session.dto.SessionResponse;
import com.skillsync.session.entity.Session;
import com.skillsync.session.entity.SessionStatus;
import com.skillsync.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;

    // ⭐ mentor creates slot
    @PostMapping("/createSlot")
    public Session createSlot(
            @RequestBody SessionRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.createSlot(dto, mentorId);
    }

    // ⭐ learner requests slot
    @PostMapping("/{id}/request")
    public Session requestSlot(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.requestSlot(id, learnerId);
    }

    // ⭐ mentor accepts
    @PostMapping("/{id}/accept")
    public Session accept(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.acceptSession(id, mentorId);
    }

    // ⭐ mentor rejects
    @PostMapping("/{id}/reject")
    public Session reject(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.rejectSession(id, mentorId);
    }

    // ⭐ learner cancels
    @PostMapping("/{id}/cancel")
    public Session cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.cancelSession(id, learnerId);
    }

    // ⭐ mentor completes
    @PostMapping("/{id}/complete")
    public Session complete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.completeSession(id, mentorId);
    }

    // ⭐ queries
    @GetMapping("/mentor/{mentorId}")
    public List<Session> mentorSessions(@PathVariable Long mentorId) {
        return service.getMentorSessions(mentorId);
    }

    @GetMapping("/learner/{learnerId}")
    public List<Session> learnerSessions(@PathVariable Long learnerId) {
        return service.getLearnerSessions(learnerId);
    }

    @GetMapping("/mentor/{mentorId}/paged")
    public Page<Session> mentorPaged(
            @PathVariable Long mentorId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getMentorSessionsPaged(mentorId, pageable);
    }

    @GetMapping("/learner/{learnerId}/paged")
    public Page<Session> learnerPaged(
            @PathVariable Long learnerId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getLearnerSessionsPaged(learnerId, pageable);
    }

    @GetMapping("/status/{status}")
    public Page<Session> statusFilter(
            @PathVariable SessionStatus status,
            Pageable pageable) {

        return service.getSessionsByStatus(status, pageable);
    }

    @GetMapping("/date-range")
    public Page<Session> dateFilter(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Pageable pageable) {

        return service.getSessionsByDateRange(start, end, pageable);
    }

    @GetMapping("/{id}/completed")
    public Boolean completed(@PathVariable Long id) {
        return service.isSessionCompleted(id);
    }

    @GetMapping("/{id}")
    public SessionResponse getSession(@PathVariable Long id) {
        return service.getSession(id);
    }
}