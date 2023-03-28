package com.github.ygyin.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {
    public static final String DELETE_QUEUE = "deleteCacheQueue";

    @Bean
    public Queue deleteCacheQueue() {
        return new Queue(DELETE_QUEUE);
    }
}
