package com.skillsync.session.service;

import com.skillsync.session.client.MentorClient;
import com.skillsync.session.dto.SessionRequestDTO;
import com.skillsync.session.dto.SessionResponse;
import com.skillsync.session.entity.Session;
import com.skillsync.session.entity.SessionStatus;
import com.skillsync.session.event.SessionEvent;
import com.skillsync.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/*
 * ================================================================
 * AUTHOR: Manideep
 * CLASS: SessionService
 * DESCRIPTION:
 * This service handles session scheduling, overlaps prevention, RabbitMQ 
 * event publishing for status updates, and session life-cycles.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository repository;
    private final MentorClient mentorClient;
    private final RabbitTemplate rabbitTemplate;

    /* ================================================================
     * METHOD: createSlot
     * DESCRIPTION:
     * Validates mentor ID, checks time overlaps, and inserts new available slots.
     * ================================================================ */
    public Session createSlot(SessionRequestDTO dto, Long userId) {

        // ⭐ fetch mentor profile id
        Long mentorId = mentorClient.getMentorProfileId(userId);
        log.info("Resolved the mentor id of user id {} as: {}", userId, mentorId);
        
        if (mentorId == null) {
            log.error("Mentor profile not found or not approved for user id: {}", userId);
            throw new RuntimeException("Mentor profile not found or not approved");
        }

        if (dto.getDurationMinutes() == null || dto.getDurationMinutes() < 10) {
            throw new RuntimeException("Minimum session duration must be 10 minutes");
        }

        if (dto.getDurationMinutes() > 240) {
            throw new RuntimeException("Session duration cannot exceed 4 hours");
        }

        if (dto.getSessionTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot create slot in past time");
        }

        LocalDateTime newStart = dto.getSessionTime();
        LocalDateTime newEnd = newStart.plusMinutes(dto.getDurationMinutes());

        List<Session> mentorSessions = repository.findByMentorId(mentorId);

        for (Session existing : mentorSessions) {

            if (existing.getStatus() == SessionStatus.CANCELLED ||
                existing.getStatus() == SessionStatus.REJECTED) {
                continue;
            }

            LocalDateTime existingStart = existing.getSessionTime();
            LocalDateTime existingEnd =
                    existingStart.plusMinutes(existing.getDurationMinutes());

            boolean overlap =
                    newStart.isBefore(existingEnd) &&
                    newEnd.isAfter(existingStart);

            if (overlap) {
                throw new RuntimeException(
                        "Slot overlaps with another scheduled session"
                );
            }
        }

        Session session = Session.builder()
                .mentorId(mentorId)
                .sessionTime(dto.getSessionTime())
                .durationMinutes(dto.getDurationMinutes())
                .status(SessionStatus.AVAILABLE)
                .build();

        return repository.save(session);
    }

    /* ================================================================
     * METHOD: requestSessionDirectly
     * DESCRIPTION:
     * Learner directly requests a session with a mentor for a specific time.
     * ================================================================ */
    @PreAuthorize("hasRole('LEARNER')")
    public Session requestSessionDirectly(SessionRequestDTO dto, Long learnerId) {
        
        if (dto.getDurationMinutes() == null || dto.getDurationMinutes() < 10) {
            throw new RuntimeException("Minimum session duration must be 10 minutes");
        }

        if (dto.getDurationMinutes() > 240) {
            throw new RuntimeException("Session duration cannot exceed 4 hours");
        }

        if (dto.getSessionTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot request a session in past time");
        }

        LocalDateTime newStart = dto.getSessionTime();
        LocalDateTime newEnd = newStart.plusMinutes(dto.getDurationMinutes());

        List<Session> mentorSessions = repository.findByMentorId(dto.getMentorId());

        for (Session existing : mentorSessions) {
            if (existing.getStatus() == SessionStatus.CANCELLED ||
                existing.getStatus() == SessionStatus.REJECTED) {
                continue;
            }

            LocalDateTime existingStart = existing.getSessionTime();
            LocalDateTime existingEnd =
                    existingStart.plusMinutes(existing.getDurationMinutes());

            LocalDateTime existingStartWithGap = existingStart.minusMinutes(15);
            LocalDateTime existingEndWithGap = existingEnd.plusMinutes(15);

            boolean overlap =
                    newStart.isBefore(existingEndWithGap) &&
                    newEnd.isAfter(existingStartWithGap);

            if (overlap) {
                throw new RuntimeException(
                        "Mentor already has a session at this time"
                );
            }
        }

        // ⭐ NEW: Check if LEARNER is double-booked
        List<Session> learnerSessions = repository.findByLearnerId(learnerId);
        for (Session existing : learnerSessions) {
            if (existing.getStatus() == SessionStatus.CANCELLED ||
                existing.getStatus() == SessionStatus.REJECTED) {
                continue;
            }

            LocalDateTime existingStart = existing.getSessionTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            LocalDateTime existingStartWithGap = existingStart.minusMinutes(15);
            LocalDateTime existingEndWithGap = existingEnd.plusMinutes(15);

            boolean overlap = newStart.isBefore(existingEndWithGap) && newEnd.isAfter(existingStartWithGap);

            if (overlap) {
                throw new RuntimeException("You already have another session booked at this time");
            }
        }

        Session session = Session.builder()
                .mentorId(dto.getMentorId())
                .learnerId(learnerId)
                .sessionTime(dto.getSessionTime())
                .durationMinutes(dto.getDurationMinutes())
                .topic(dto.getTopic() != null ? dto.getTopic() : "General Guidance")
                .status(SessionStatus.REQUESTED)
                .build();

        Session saved = repository.save(session);
        log.info("🔥 BEFORE PUBLISH EVENT for direct session request");
        publishEvent(saved);

        return saved;
    }

    /* ================================================================
     * METHOD: requestSlot
     * DESCRIPTION:
     * Associates an available session slot with a learner's ID.
     * ================================================================ */
    @PreAuthorize("hasRole('LEARNER')")
    public Session requestSlot(Long sessionId, Long learnerId) {

        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session slot not found"));

        if (session.getStatus() != SessionStatus.AVAILABLE) {
            throw new RuntimeException("Slot not available for booking");
        }

        session.setLearnerId(learnerId);
        session.setStatus(SessionStatus.REQUESTED);

        Session saved = repository.save(session);
        log.info("🔥 BEFORE PUBLISH EVENT for session request");
        publishEvent(saved);

        return saved;
    }

    /* ================================================================
     * METHOD: acceptSession
     * DESCRIPTION:
     * Accepts a learner's slot request exclusively if invoked by the owner mentor.
     * ================================================================ */
    public Session acceptSession(Long id, Long userId, String meetingLink) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Long mentorProfileId = mentorClient.getMentorProfileId(userId);

        if (!session.getMentorId().equals(mentorProfileId)) {
            throw new RuntimeException("You can accept only your sessions");
        }

        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new RuntimeException("Only requested sessions can be accepted");
        }

        session.setStatus(SessionStatus.ACCEPTED);
        if (meetingLink != null && !meetingLink.trim().isEmpty()) {
            session.setMeetingLink(meetingLink);
        }

        Session saved = repository.save(session);
        log.info("🔥 BEFORE PUBLISH EVENT for session acceptance");
        publishEvent(saved);

        return saved;
    }

    /* ================================================================
     * METHOD: rejectSession
     * DESCRIPTION:
     * Rejects an ongoing request putting the slot back to its resolved state.
     * ================================================================ */
    public Session rejectSession(Long id, Long userId, String reason) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Long mentorProfileId = mentorClient.getMentorProfileId(userId);

        if (!session.getMentorId().equals(mentorProfileId)) {
            throw new RuntimeException("You can reject only your sessions");
        }

        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new RuntimeException("Only requested sessions can be rejected");
        }

        session.setStatus(SessionStatus.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            session.setRejectionReason(reason);
        }
        
        return repository.save(session);
    }

    /* ================================================================
     * METHOD: cancelSession
     * DESCRIPTION:
     * Cancels an already booked session by the learner themselves.
     * ================================================================ */
    public Session cancelSession(Long id, Long learnerId) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getLearnerId() == null ||
            !session.getLearnerId().equals(learnerId)) {
            throw new RuntimeException("You can cancel only your booked session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Completed session cannot be cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return repository.save(session);
    }

    /* ================================================================
     * METHOD: completeSession
     * DESCRIPTION:
     * Concludes a successfully hosted session by resolving its terminal status.
     * ================================================================ */
    public Session completeSession(Long id, Long userId) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ACCEPTED) {
            throw new RuntimeException("Only accepted sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);

        Session saved = repository.save(session);
        log.info("🔥 BEFORE PUBLISH EVENT for session completion");
        publishEvent(saved);

        return saved;
    }

    /* ================================================================
     * METHOD: publishEvent
     * DESCRIPTION:
     * Helper emitting asynchronous RabbitMQ metadata describing session mutations.
     * ================================================================ */
    private void publishEvent(Session session) {
    	log.info("🔥 publishEvent CALLED for session id: {}", session.getId());
        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .status(session.getStatus().name())
                .sessionTime(session.getSessionTime())
                .build();

        try {
            rabbitTemplate.convertAndSend("session.queue", event);
            log.info("📤 EVENT SENT TO RABBITMQ: {}", event);
        } catch (Exception e) {
            log.error("❌ Failed to send event to RabbitMQ (is it running?): {}", e.getMessage());
        }
    }

    /* ================================================================
     * METHOD: autoCompleteSessions
     * DESCRIPTION:
     * Automatically transitions ACCEPTED sessions to COMPLETED if their end time is in the past.
     * ================================================================ */
    private void autoCompleteSessions(List<Session> sessions) {
        LocalDateTime now = LocalDateTime.now();
        for (Session session : sessions) {
            if (session.getStatus() == SessionStatus.ACCEPTED) {
                LocalDateTime endTime = session.getSessionTime().plusMinutes(session.getDurationMinutes());
                if (endTime.isBefore(now)) {
                    session.setStatus(SessionStatus.COMPLETED);
                    repository.save(session);
                    publishEvent(session);
                }
            }
        }
    }

    /* ================================================================
     * METHOD: getLearnerSessions
     * DESCRIPTION:
     * Wrapper fetching full list representing the learner's booked histories.
     * ================================================================ */
    public List<Session> getLearnerSessions(Long learnerId) {
        List<Session> sessions = repository.findByLearnerId(learnerId);
        autoCompleteSessions(sessions);
        return sessions;
    }

    /* ================================================================
     * METHOD: getMentorSessions
     * DESCRIPTION:
     * Wrapper fetching full list representing the mentor's hosted slots.
     * ================================================================ */
    public List<Session> getMentorSessions(Long mentorId) {
        List<Session> sessions = repository.findByMentorId(mentorId);
        autoCompleteSessions(sessions);
        return sessions;
    }

    /* ================================================================
     * METHOD: getMentorSessionsPaged
     * DESCRIPTION:
     * Facilitates chunked/paged returning of the mentor slots array.
     * ================================================================ */
    public Page<Session> getMentorSessionsPaged(Long mentorId, Pageable pageable) {
        return repository.findByMentorId(mentorId, pageable);
    }

    /* ================================================================
     * METHOD: getLearnerSessionsPaged
     * DESCRIPTION:
     * Facilitates chunked/paged returning of the learner booked array.
     * ================================================================ */
    public Page<Session> getLearnerSessionsPaged(Long learnerId, Pageable pageable) {
        return repository.findByLearnerId(learnerId, pageable);
    }

    /* ================================================================
     * METHOD: getSessionsByStatus
     * DESCRIPTION:
     * Filters instances globally fetching exclusively those sharing status.
     * ================================================================ */
    public Page<Session> getSessionsByStatus(SessionStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    /* ================================================================
     * METHOD: getSessionsByDateRange
     * DESCRIPTION:
     * Retrieves sessions occurring strictly between mapped date/times.
     * ================================================================ */
    public Page<Session> getSessionsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {

        return repository.findBySessionTimeBetween(start, end, pageable);
    }

    /* ================================================================
     * METHOD: isSessionCompleted
     * DESCRIPTION:
     * Internal boolean check mapping terminal states correctly.
     * ================================================================ */
    public Boolean isSessionCompleted(Long sessionId) {

        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return session.getStatus() == SessionStatus.COMPLETED;
    }

    /* ================================================================
     * METHOD: getSession
     * DESCRIPTION:
     * Constructs and returns isolated Response mappings of explicit sessions.
     * ================================================================ */
    public SessionResponse getSession(Long id) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return SessionResponse.builder()
                .id(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .status(session.getStatus().name())
                .build();
    }

    /* ================================================================
     * METHOD: autoCancelExpiredRequests
     * DESCRIPTION:
     * Scheduled job that automatically cancels sessions that are still 
     * REQUESTED but whose start time has passed.
     * ================================================================ */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 * * * * *") // Runs every minute
    public void autoCancelExpiredRequests() {
        List<Session> expiredSessions = repository.findByStatusAndSessionTimeBefore(
            SessionStatus.REQUESTED, LocalDateTime.now()
        );

        for (Session session : expiredSessions) {
            log.info("Auto-cancelling expired session id: {}", session.getId());
            session.setStatus(SessionStatus.CANCELLED);
            Session saved = repository.save(session);
            publishEvent(saved);
        }
    }
}