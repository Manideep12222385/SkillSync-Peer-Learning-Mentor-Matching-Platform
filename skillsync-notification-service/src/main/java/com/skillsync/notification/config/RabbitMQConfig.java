package com.skillsync.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.*;

import feign.RequestInterceptor;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue sessionQueue() {
        return new Queue("session.queue", true);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    
}