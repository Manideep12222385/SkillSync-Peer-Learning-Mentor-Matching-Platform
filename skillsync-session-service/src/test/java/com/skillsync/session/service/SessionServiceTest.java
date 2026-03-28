package com.skillsync.session.service;

import com.skillsync.session.client.MentorClient;
import com.skillsync.session.dto.SessionRequestDTO;
import com.skillsync.session.dto.SessionResponse;
import com.skillsync.session.entity.Session;
import com.skillsync.session.entity.SessionStatus;
import com.skillsync.session.event.SessionEvent;
import com.skillsync.session.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MentorClient mentorClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;
    private SessionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testSession = Session.builder()
                .id(100L)
                .mentorId(10L)
                .learnerId(20L)
                .sessionTime(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .status(SessionStatus.AVAILABLE)
                .build();

        requestDTO = new SessionRequestDTO();
        requestDTO.setSessionTime(LocalDateTime.now().plusDays(1));
        requestDTO.setDurationMinutes(60);
    }

    // --- CREATE SLOT TESTS ---
    @Test
    void testCreateSlot_Success() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        when(sessionRepository.findByMentorId(10L)).thenReturn(Collections.emptyList());
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.createSlot(requestDTO, 1L);

        assertNotNull(session);
        assertEquals(10L, session.getMentorId());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void testCreateSlot_MentorNotFound() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.createSlot(requestDTO, 1L));
        assertEquals("Mentor profile not found or not approved", ex.getMessage());
    }

    @Test
    void testCreateSlot_DurationTooShort() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        requestDTO.setDurationMinutes(5);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.createSlot(requestDTO, 1L));
        assertEquals("Minimum session duration must be 10 minutes", ex.getMessage());
    }

    @Test
    void testCreateSlot_DurationTooLong() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        requestDTO.setDurationMinutes(300);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.createSlot(requestDTO, 1L));
        assertEquals("Session duration cannot exceed 4 hours", ex.getMessage());
    }

    @Test
    void testCreateSlot_PastTime() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        requestDTO.setSessionTime(LocalDateTime.now().minusDays(1));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.createSlot(requestDTO, 1L));
        assertEquals("Cannot create slot in past time", ex.getMessage());
    }

    @Test
    void testCreateSlot_Overlap() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        Session existing = Session.builder()
                .sessionTime(requestDTO.getSessionTime())
                .durationMinutes(60)
                .status(SessionStatus.AVAILABLE)
                .build();
        when(sessionRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.createSlot(requestDTO, 1L));
        assertEquals("Slot overlaps with another scheduled session", ex.getMessage());
    }
    
    @Test
    void testCreateSlot_OverlapIgnoredCancelled() {
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        Session existing = Session.builder()
                .sessionTime(requestDTO.getSessionTime())
                .durationMinutes(60)
                .status(SessionStatus.CANCELLED)
                .build();
        when(sessionRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(existing));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.createSlot(requestDTO, 1L);
        assertNotNull(session);
    }

    // --- REQUEST SLOT TESTS ---
    @Test
    void testRequestSlot_Success() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession); // returns modified

        Session session = sessionService.requestSlot(100L, 20L);

        assertEquals(SessionStatus.REQUESTED, session.getStatus());
        assertEquals(20L, session.getLearnerId());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("session.queue"), any(SessionEvent.class));
    }

    @Test
    void testRequestSlot_NotFound() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.requestSlot(100L, 20L));
        assertEquals("Session slot not found", ex.getMessage());
    }

    @Test
    void testRequestSlot_NotAvailable() {
        testSession.setStatus(SessionStatus.REQUESTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.requestSlot(100L, 20L));
        assertEquals("Slot not available for booking", ex.getMessage());
    }

    // --- ACCEPT SESSION TESTS ---
    @Test
    void testAcceptSession_Success() {
        testSession.setStatus(SessionStatus.REQUESTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.acceptSession(100L, 1L);

        assertEquals(SessionStatus.ACCEPTED, session.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("session.queue"), any(SessionEvent.class));
    }

    @Test
    void testAcceptSession_NotYourSession() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(99L); // Wrong mentor!

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.acceptSession(100L, 1L));
        assertEquals("You can accept only your sessions", ex.getMessage());
    }

    @Test
    void testAcceptSession_NotRequestedStatus() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession)); // AVAILABLE
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.acceptSession(100L, 1L));
        assertEquals("Only requested sessions can be accepted", ex.getMessage());
    }

    // --- REJECT SESSION TESTS ---
    @Test
    void testRejectSession_Success() {
        testSession.setStatus(SessionStatus.REQUESTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.rejectSession(100L, 1L);

        assertEquals(SessionStatus.REJECTED, session.getStatus());
        verify(sessionRepository, times(1)).save(testSession);
    }
    
    @Test
    void testRejectSession_NotRequested() {
        testSession.setStatus(SessionStatus.AVAILABLE);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.rejectSession(100L, 1L));
        assertEquals("Only requested sessions can be rejected", ex.getMessage());
    }
    
    @Test
    void testRejectSession_NotYours() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(99L);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.rejectSession(100L, 1L));
        assertEquals("You can reject only your sessions", ex.getMessage());
    }

    // --- CANCEL SESSION TESTS ---
    @Test
    void testCancelSession_Success() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.cancelSession(100L, 20L);

        assertEquals(SessionStatus.CANCELLED, session.getStatus());
    }

    @Test
    void testCancelSession_NotYours() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.cancelSession(100L, 99L));
        assertEquals("You can cancel only your booked session", ex.getMessage());
    }

    @Test
    void testCancelSession_AlreadyCompleted() {
        testSession.setStatus(SessionStatus.COMPLETED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.cancelSession(100L, 20L));
        assertEquals("Completed session cannot be cancelled", ex.getMessage());
    }

    // --- COMPLETE SESSION TESTS ---
    @Test
    void testCompleteSession_Success() {
        testSession.setStatus(SessionStatus.ACCEPTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session session = sessionService.completeSession(100L, 1L);

        assertEquals(SessionStatus.COMPLETED, session.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("session.queue"), any(SessionEvent.class));
    }
    
    @Test
    void testCompleteSession_NotYours() {
        testSession.setStatus(SessionStatus.ACCEPTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(99L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.completeSession(100L, 1L));
        assertEquals("You can complete only your sessions", ex.getMessage());
    }
    
    @Test
    void testCompleteSession_NotAccepted() {
        testSession.setStatus(SessionStatus.REQUESTED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        when(mentorClient.getMentorProfileId(1L)).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.completeSession(100L, 1L));
        assertEquals("Only accepted sessions can be completed", ex.getMessage());
    }

    // --- GETTERS & UTILITIES TESTS ---
    @Test
    void testGetLearnerSessions() {
        when(sessionRepository.findByLearnerId(20L)).thenReturn(Collections.singletonList(testSession));
        List<Session> res = sessionService.getLearnerSessions(20L);
        assertEquals(1, res.size());
    }

    @Test
    void testGetMentorSessions() {
        when(sessionRepository.findByMentorId(10L)).thenReturn(Collections.singletonList(testSession));
        List<Session> res = sessionService.getMentorSessions(10L);
        assertEquals(1, res.size());
    }

    @Test
    void testGetMentorSessionsPaged() {
        Page<Session> page = new PageImpl<>(Collections.singletonList(testSession));
        when(sessionRepository.findByMentorId(eq(10L), any(Pageable.class))).thenReturn(page);
        Page<Session> res = sessionService.getMentorSessionsPaged(10L, Pageable.unpaged());
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void testGetLearnerSessionsPaged() {
        Page<Session> page = new PageImpl<>(Collections.singletonList(testSession));
        when(sessionRepository.findByLearnerId(eq(20L), any(Pageable.class))).thenReturn(page);
        Page<Session> res = sessionService.getLearnerSessionsPaged(20L, Pageable.unpaged());
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void testGetSessionsByStatus() {
        Page<Session> page = new PageImpl<>(Collections.singletonList(testSession));
        when(sessionRepository.findByStatus(eq(SessionStatus.AVAILABLE), any(Pageable.class))).thenReturn(page);
        Page<Session> res = sessionService.getSessionsByStatus(SessionStatus.AVAILABLE, Pageable.unpaged());
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void testGetSessionsByDateRange() {
        Page<Session> page = new PageImpl<>(Collections.singletonList(testSession));
        when(sessionRepository.findBySessionTimeBetween(any(), any(), any())).thenReturn(page);
        Page<Session> res = sessionService.getSessionsByDateRange(LocalDateTime.now(), LocalDateTime.now().plusDays(2), Pageable.unpaged());
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void testIsSessionCompleted() {
        testSession.setStatus(SessionStatus.COMPLETED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        assertTrue(sessionService.isSessionCompleted(100L));
        
        testSession.setStatus(SessionStatus.AVAILABLE);
        assertFalse(sessionService.isSessionCompleted(100L));
    }

    @Test
    void testGetSession() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(testSession));
        SessionResponse res = sessionService.getSession(100L);
        assertNotNull(res);
        assertEquals(10L, res.getMentorId());
        assertEquals("AVAILABLE", res.getStatus());
    }
}
