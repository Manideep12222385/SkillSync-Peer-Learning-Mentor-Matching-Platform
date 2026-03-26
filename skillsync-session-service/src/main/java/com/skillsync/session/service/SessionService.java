package com.skillsync.session.service;

import com.skillsync.session.client.MentorClient;
import com.skillsync.session.dto.SessionRequestDTO;
import com.skillsync.session.dto.SessionResponse;
import com.skillsync.session.entity.Session;
import com.skillsync.session.entity.SessionStatus;
import com.skillsync.session.event.SessionEvent;
import com.skillsync.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repository;
    private final MentorClient mentorClient;
    private final RabbitTemplate rabbitTemplate;

    // ⭐ MENTOR CREATES SLOT
    public Session createSlot(SessionRequestDTO dto, Long userId) {

        // ⭐ fetch mentor profile id
        Long mentorId = mentorClient.getMentorProfileId(userId);
        System.out.println("Resolved the mentor id of user id "+userId+"as: "+mentorId);
        
        if (mentorId == null) {
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

    // ⭐ LEARNER REQUESTS SLOT
    public Session requestSlot(Long sessionId, Long learnerId) {

        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session slot not found"));

        if (session.getStatus() != SessionStatus.AVAILABLE) {
            throw new RuntimeException("Slot not available for booking");
        }

        session.setLearnerId(learnerId);
        session.setStatus(SessionStatus.REQUESTED);

        Session saved = repository.save(session);
        System.out.println("🔥 BEFORE PUBLISH EVENT");
        publishEvent(saved);

        return saved;
    }

    // ⭐ MENTOR ACCEPTS
    public Session acceptSession(Long id, Long userId) {

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

        Session saved = repository.save(session);
        System.out.println("🔥 BEFORE PUBLISH EVENT");
        publishEvent(saved);

        return saved;
    }

    // ⭐ MENTOR REJECTS
    public Session rejectSession(Long id, Long userId) {

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
        return repository.save(session);
    }

    // ⭐ LEARNER CANCELS
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

    // ⭐ MENTOR COMPLETES
    public Session completeSession(Long id, Long userId) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Long mentorProfileId = mentorClient.getMentorProfileId(userId);

        if (!session.getMentorId().equals(mentorProfileId)) {
            throw new RuntimeException("You can complete only your sessions");
        }

        if (session.getStatus() != SessionStatus.ACCEPTED) {
            throw new RuntimeException("Only accepted sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);

        Session saved = repository.save(session);
        System.out.println("🔥 BEFORE PUBLISH EVENT");
        publishEvent(saved);

        return saved;
    }

    private void publishEvent(Session session) {
    	System.out.println("🔥 publishEvent CALLED");
        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .status(session.getStatus().name())
                .sessionTime(session.getSessionTime())
                .build();

        rabbitTemplate.convertAndSend("session.queue", event);
        System.out.println("📤 EVENT SENT TO RABBITMQ: " + event);
    }

    public List<Session> getLearnerSessions(Long learnerId) {
        return repository.findByLearnerId(learnerId);
    }

    public List<Session> getMentorSessions(Long mentorId) {
        return repository.findByMentorId(mentorId);
    }

    public Page<Session> getMentorSessionsPaged(Long mentorId, Pageable pageable) {
        return repository.findByMentorId(mentorId, pageable);
    }

    public Page<Session> getLearnerSessionsPaged(Long learnerId, Pageable pageable) {
        return repository.findByLearnerId(learnerId, pageable);
    }

    public Page<Session> getSessionsByStatus(SessionStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    public Page<Session> getSessionsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {

        return repository.findBySessionTimeBetween(start, end, pageable);
    }

    public Boolean isSessionCompleted(Long sessionId) {

        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return session.getStatus() == SessionStatus.COMPLETED;
    }

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
}