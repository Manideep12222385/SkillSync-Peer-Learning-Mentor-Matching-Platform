package com.skillsync.notification.listener;

import com.skillsync.notification.client.AuthClient;
import com.skillsync.notification.client.MentorClient;
import com.skillsync.notification.event.SessionEvent;
import com.skillsync.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEventListener {

    private final EmailService emailService;
    private final AuthClient authClient;
    private final MentorClient mentorClient;

    @RabbitListener(queues = "session.queue")
    public void handleSessionEvent(SessionEvent event) {

        log.info("📩 Session Event Received: {}", event);

        try {
            Long learnerId = event.getLearnerId();
            Long mentorRecordId = event.getMentorId();
            
            if(learnerId == null || mentorRecordId == null) return;

            Long mentorUserId = mentorClient.getUserIdByMentorId(mentorRecordId);

            String learnerEmail = authClient.getUserEmail(learnerId);
            String mentorEmail = authClient.getUserEmail(mentorUserId);
            String learnerName = authClient.getUserName(learnerId);
            String mentorName = authClient.getUserName(mentorUserId);

            String time = event.getSessionTime() != null ? event.getSessionTime().toString() : "TBD";

            switch (event.getStatus()) {
                case "REQUESTED":
                    // Send to Mentor
                    emailService.sendEmail(mentorEmail, "New Session Request", 
                        "<h2>New Session Request!</h2><p>Hello " + mentorName + ",</p><p>You have a new session request from " + learnerName + " for " + time + ".</p><p>Please login to your dashboard to accept or reject it.</p>");
                    // Send to Learner
                    emailService.sendEmail(learnerEmail, "Session Booked", 
                        "<h2>Session Booked Successfully!</h2><p>Hello " + learnerName + ",</p><p>Your session slot for " + time + " has been booked and is pending mentor approval.</p>");
                    break;
                case "ACCEPTED":
                    emailService.sendEmail(learnerEmail, "Session Accepted!", 
                        "<h2>Session Accepted!</h2><p>Hello " + learnerName + ",</p><p>Great news! " + mentorName + " has accepted your session scheduled for " + time + ".</p>");
                    break;
                case "REMINDER":
                    emailService.sendEmail(learnerEmail, "Session Reminder", 
                        "<h2>Upcoming Session Reminder</h2><p>Your session with " + mentorName + " is starting soon!</p>");
                    emailService.sendEmail(mentorEmail, "Session Reminder", 
                        "<h2>Upcoming Session Reminder</h2><p>Your session with " + learnerName + " is starting soon!</p>");
                    break;
            }
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }
}
