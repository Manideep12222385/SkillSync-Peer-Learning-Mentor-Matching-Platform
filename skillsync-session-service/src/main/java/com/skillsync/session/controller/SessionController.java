package com.skillsync.session.controller;

import com.skillsync.session.dto.SessionRequestDTO;
import com.skillsync.session.entity.Session;
import com.skillsync.session.entity.SessionStatus;
import com.skillsync.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    // ⭐ BOOK SESSION
    @PostMapping
    public Session request(@RequestBody SessionRequestDTO dto) {
        return service.requestSession(dto);
    }

    // ⭐ ACCEPT
    @PutMapping("/{id}/accept")
    public Session accept(@PathVariable Long id) {
        return service.acceptSession(id);
    }

    // ⭐ REJECT
    @PutMapping("/{id}/reject")
    public Session reject(@PathVariable Long id) {
        return service.rejectSession(id);
    }

    // ⭐ CANCEL
    @PutMapping("/{id}/cancel")
    public Session cancel(@PathVariable Long id) {
        return service.cancelSession(id);
    }

    // ⭐ COMPLETE
    @PutMapping("/{id}/complete")
    public Session complete(@PathVariable Long id) {
        return service.completeSession(id);
    }

    // ⭐ LEARNER HISTORY (simple)
    @GetMapping("/learner/{learnerId}")
    public List<Session> learnerSessions(@PathVariable Long learnerId) {
        return service.getLearnerSessions(learnerId);
    }

    // ⭐ MENTOR DASHBOARD (simple)
    @GetMapping("/mentor/{mentorId}")
    public List<Session> mentorSessions(@PathVariable Long mentorId) {
        return service.getMentorSessions(mentorId);
    }

    // ⭐ PAGINATION — mentor
    @GetMapping("/mentor/{mentorId}/paged")
    public Page<Session> mentorSessionsPaged(
            @PathVariable Long mentorId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getMentorSessionsPaged(mentorId, pageable);
    }

    // ⭐ PAGINATION — learner
    @GetMapping("/learner/{learnerId}/paged")
    public Page<Session> learnerSessionsPaged(
            @PathVariable Long learnerId,
            @PageableDefault(size = 5) Pageable pageable) {

        return service.getLearnerSessionsPaged(learnerId, pageable);
    }

    // ⭐ FILTER BY STATUS
    @GetMapping("/status/{status}")
    public Page<Session> sessionsByStatus(
            @PathVariable SessionStatus status,
            Pageable pageable) {

        return service.getSessionsByStatus(status, pageable);
    }

    // ⭐ FILTER BY DATE RANGE
    @GetMapping("/date-range")
    public Page<Session> sessionsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Pageable pageable) {

        return service.getSessionsByDateRange(start, end, pageable);
    }
    
    @GetMapping("/{id}/completed")
    public Boolean isCompleted(@PathVariable Long id) {
        return service.isSessionCompleted(id);
    }
}