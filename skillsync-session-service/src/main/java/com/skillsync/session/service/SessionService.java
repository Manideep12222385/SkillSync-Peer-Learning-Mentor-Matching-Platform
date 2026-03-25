package com.skillsync.session.service;

import com.skillsync.session.client.MentorClient;
import com.skillsync.session.client.UserClient;
import com.skillsync.session.dto.SessionRequestDTO;
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
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    // ⭐ BOOK SESSION
    public Session requestSession(SessionRequestDTO dto) {

        Boolean mentorValid = mentorClient.mentorExists(dto.getMentorId());
        if (mentorValid == null || !mentorValid) {
            throw new RuntimeException("Mentor not found");
        }

        Boolean learnerValid = userClient.userExists(dto.getLearnerId());
        if (learnerValid == null || !learnerValid) {
            throw new RuntimeException("Learner profile not found");
        }

        LocalDateTime start = dto.getSessionTime().minusMinutes(60);
        LocalDateTime end = dto.getSessionTime().plusMinutes(60);

        List<Session> conflicts =
                repository.findByMentorIdAndSessionTimeBetween(
                        dto.getMentorId(), start, end);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Mentor not available at this time");
        }

        Session session = Session.builder()
                .mentorId(dto.getMentorId())
                .learnerId(dto.getLearnerId())
                .sessionTime(dto.getSessionTime())
                .durationMinutes(dto.getDurationMinutes())
                .status(SessionStatus.REQUESTED)
                .build();

        Session saved = repository.save(session);
        publishEvent(saved);

        return saved;
    }

    // ⭐ ACCEPT
    public Session acceptSession(Long id) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new RuntimeException("Only requested sessions can be accepted");
        }

        session.setStatus(SessionStatus.ACCEPTED);
        Session saved = repository.save(session);
        publishEvent(saved);

        return saved;
    }

    // ⭐ REJECT
    public Session rejectSession(Long id) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new RuntimeException("Only requested sessions can be rejected");
        }

        session.setStatus(SessionStatus.REJECTED);
        return repository.save(session);
    }

    // ⭐ CANCEL
    public Session cancelSession(Long id) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Completed session cannot be cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return repository.save(session);
    }

    // ⭐ COMPLETE
    public Session completeSession(Long id) {

        Session session = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ACCEPTED) {
            throw new RuntimeException("Only accepted sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        Session saved = repository.save(session);
        publishEvent(saved);

        return saved;
    }

    // ⭐ EVENT PUBLISHER
    private void publishEvent(Session session) {

        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .status(session.getStatus().name())
                .sessionTime(session.getSessionTime())
                .build();

        rabbitTemplate.convertAndSend("session.queue", event);
    }

    // ⭐ SIMPLE QUERIES
    public List<Session> getLearnerSessions(Long learnerId) {
        return repository.findByLearnerId(learnerId);
    }

    public List<Session> getMentorSessions(Long mentorId) {
        return repository.findByMentorId(mentorId);
    }

    // ⭐ PAGINATION
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
}