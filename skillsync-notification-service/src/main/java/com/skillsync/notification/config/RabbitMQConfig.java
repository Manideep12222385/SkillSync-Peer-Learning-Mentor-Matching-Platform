package com.skillsync.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue sessionQueue() {
        return new Queue("session.queue", true);
    }
}