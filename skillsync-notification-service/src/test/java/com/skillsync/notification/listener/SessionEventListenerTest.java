package com.skillsync.notification.listener;

import com.skillsync.notification.client.AuthClient;
import com.skillsync.notification.client.MentorClient;
import com.skillsync.notification.event.SessionEvent;
import com.skillsync.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AuthClient authClient;

    @Mock
    private MentorClient mentorClient;

    @InjectMocks
    private SessionEventListener listener;

    private SessionEvent event;

    @BeforeEach
    void setUp() {
        event = new SessionEvent();
        event.setSessionId(1L);
        event.setLearnerId(20L);
        event.setMentorId(100L); // mentor record id
        event.setSessionTime(LocalDateTime.now());
    }

    private void mockClients() {
        when(mentorClient.getUserIdByMentorId(100L)).thenReturn(10L);
        when(authClient.getUserEmail(20L)).thenReturn("learner@example.com");
        when(authClient.getUserEmail(10L)).thenReturn("mentor@example.com");
        when(authClient.getUserName(20L)).thenReturn("LearnerBob");
        when(authClient.getUserName(10L)).thenReturn("MentorAlice");
    }

    @Test
    void testHandleSessionEvent_Requested() {
        mockClients();
        event.setStatus("REQUESTED");

        listener.handleSessionEvent(event);

        verify(emailService, times(1)).sendEmail(eq("mentor@example.com"), eq("New Session Request"), anyString());
        verify(emailService, times(1)).sendEmail(eq("learner@example.com"), eq("Session Booked"), anyString());
    }

    @Test
    void testHandleSessionEvent_Accepted() {
        mockClients();
        event.setStatus("ACCEPTED");

        listener.handleSessionEvent(event);

        verify(emailService, times(1)).sendEmail(eq("learner@example.com"), eq("Session Accepted!"), anyString());
    }

    @Test
    void testHandleSessionEvent_Reminder() {
        mockClients();
        event.setStatus("REMINDER");

        listener.handleSessionEvent(event);

        verify(emailService, times(1)).sendEmail(eq("learner@example.com"), eq("Session Reminder"), anyString());
        verify(emailService, times(1)).sendEmail(eq("mentor@example.com"), eq("Session Reminder"), anyString());
    }

    @Test
    void testHandleSessionEvent_NullIdsReturnEarly() {
        event.setLearnerId(null);
        listener.handleSessionEvent(event);
        verifyNoInteractions(mentorClient, authClient, emailService);

        event.setLearnerId(20L);
        event.setMentorId(null);
        listener.handleSessionEvent(event);
        verifyNoInteractions(mentorClient, authClient, emailService);
    }

    @Test
    void testHandleSessionEvent_ExceptionCaught() {
        event.setStatus("REQUESTED");
        when(mentorClient.getUserIdByMentorId(100L)).thenThrow(new RuntimeException("API Failure"));

        // Should not throw exception outwards
        listener.handleSessionEvent(event);

        verify(mentorClient, times(1)).getUserIdByMentorId(100L);
        verifyNoInteractions(emailService);
    }

    @Test
    void testHandleSessionEvent_NullTime() {
        mockClients();
        event.setStatus("ACCEPTED");
        event.setSessionTime(null);

        listener.handleSessionEvent(event);

        verify(emailService, times(1)).sendEmail(eq("learner@example.com"), eq("Session Accepted!"), contains("TBD"));
    }
}
