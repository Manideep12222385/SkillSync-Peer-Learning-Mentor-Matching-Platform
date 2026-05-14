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
import com.skillsync.session.client.MentorClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDateTime;
import java.util.List;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: SessionController
 * DESCRIPTION:
 * This controller handles session-related operations including creating,
 * requesting, accepting, rejecting, completing sessions, and fetching
 * filtered paginated sessions.
 * ================================================================
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;
    private final MentorClient mentorClient;

    /* ================================================================
     * METHOD: createSlot
     * DESCRIPTION:
     * Mentors can create available time slots for future sessions.
     * ================================================================ */
    @PostMapping("/createSlot")
    public Session createSlot(
            @RequestBody SessionRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.createSlot(dto, mentorId);
    }

    /* ================================================================
     * METHOD: requestDirect
     * DESCRIPTION:
     * Learners can request a session directly without an existing slot.
     * ================================================================ */
    @PostMapping("/requestDirect")
    public Session requestDirect(
            @RequestBody SessionRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.requestSessionDirectly(dto, learnerId);
    }

    /* ================================================================
     * METHOD: requestSlot
     * DESCRIPTION:
     * Learners can request an available slot to book a session.
     * ================================================================ */
    @PostMapping("/{id}/request")
    public Session requestSlot(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.requestSlot(id, learnerId);
    }

    /* ================================================================
     * METHOD: accept
     * DESCRIPTION:
     * Mentors can accept a learner's session request.
     * ================================================================ */
    @PostMapping("/{id}/accept")
    public Session accept(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        String link = payload != null ? payload.get("meetingLink") : null;
        return service.acceptSession(id, mentorId, link);
    }

    /* ================================================================
     * METHOD: reject
     * DESCRIPTION:
     * Mentors can reject a learner's session request.
     * ================================================================ */
    @PostMapping("/{id}/reject")
    public Session reject(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        String reason = payload != null ? payload.get("reason") : null;
        return service.rejectSession(id, mentorId, reason);
    }

    /* ================================================================
     * METHOD: cancel
     * DESCRIPTION:
     * Learners can cancel a booked session request before it happens.
     * ================================================================ */
    @PostMapping("/{id}/cancel")
    public Session cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long learnerId = ((Number) jwt.getClaim("userId")).longValue();
        return service.cancelSession(id, learnerId);
    }

    /* ================================================================
     * METHOD: complete
     * DESCRIPTION:
     * Mentors mark a session as complete after it has successfully finished.
     * ================================================================ */
    @PostMapping("/{id}/complete")
    public Session complete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long mentorId = ((Number) jwt.getClaim("userId")).longValue();
        return service.completeSession(id, mentorId);
    }

    /* ================================================================
     * METHOD: mentorSessions
     * DESCRIPTION:
     * Retrieves all sessions for a specific mentor without pagination.
     * ================================================================ */
    @GetMapping("/mentor/{userId}")
    public List<Session> mentorSessions(@PathVariable Long userId) {
        Long mentorId = mentorClient.getMentorProfileId(userId);
        if (mentorId == null) {
            return List.of();
        }
        return service.getMentorSessions(mentorId);
    }

    /* ================================================================
     * METHOD: learnerSessions
     * DESCRIPTION:
     * Retrieves all sessions for a specific learner without pagination.
     * ================================================================ */
    @GetMapping("/learner/{learnerId}")
    public List<Session> learnerSessions(@PathVariable Long learnerId) {
        return service.getLearnerSessions(learnerId);
    }

    /* ================================================================
     * METHOD: mentorPaged
     * DESCRIPTION:
     * Retrieves sessions for a specific mentor with pagination.
     * ================================================================ */
    @GetMapping("/mentor/{mentorId}/paged")
    public Page<Session> mentorPaged(
            @PathVariable Long mentorId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getMentorSessionsPaged(mentorId, pageable);
    }

    /* ================================================================
     * METHOD: learnerPaged
     * DESCRIPTION:
     * Retrieves sessions for a specific learner with pagination.
     * ================================================================ */
    @GetMapping("/learner/{learnerId}/paged")
    public Page<Session> learnerPaged(
            @PathVariable Long learnerId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getLearnerSessionsPaged(learnerId, pageable);
    }

    /* ================================================================
     * METHOD: statusFilter
     * DESCRIPTION:
     * Retrieves paged sessions by a specific status globally.
     * ================================================================ */
    @GetMapping("/status/{status}")
    public Page<Session> statusFilter(
            @PathVariable SessionStatus status,
            Pageable pageable) {

        return service.getSessionsByStatus(status, pageable);
    }

    /* ================================================================
     * METHOD: dateFilter
     * DESCRIPTION:
     * Retrieves paged sessions filtered by a start and end date range.
     * ================================================================ */
    @GetMapping("/date-range")
    public Page<Session> dateFilter(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Pageable pageable) {

        return service.getSessionsByDateRange(start, end, pageable);
    }

    /* ================================================================
     * METHOD: completed
     * DESCRIPTION:
     * Determines whether a specific session is marked as completed.
     * ================================================================ */
    @GetMapping("/{id}/completed")
    public Boolean completed(@PathVariable Long id) {
        return service.isSessionCompleted(id);
    }

    /* ================================================================
     * METHOD: getSession
     * DESCRIPTION:
     * Retrieves the details of a specific session by its internal ID.
     * ================================================================ */
    @GetMapping("/{id}")
    public SessionResponse getSession(@PathVariable Long id) {
        return service.getSession(id);
    }
}