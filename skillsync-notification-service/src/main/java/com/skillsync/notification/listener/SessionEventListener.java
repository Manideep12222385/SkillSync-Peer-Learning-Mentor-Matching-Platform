package com.skillsync.notification.listener;

import com.skillsync.notification.event.SessionEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener {

    @RabbitListener(queues = "session.queue")
    public void handleSessionEvent(SessionEvent event) {

        System.out.println("📩 Notification Received:");
        System.out.println("Session ID: " + event.getSessionId());
        System.out.println("Status: " + event.getStatus());
        System.out.println("Mentor: " + event.getMentorId());
        System.out.println("Learner: " + event.getLearnerId());
        System.out.println("Time: " + event.getSessionTime());

        // later email / push logic
    }
}