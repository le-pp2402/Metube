package com.phatpl.metube.configs;


import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${QUEUE_HOST}")
    private String QUEUE_HOST;

    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(QUEUE_HOST);
        return connectionFactory;
    }
}
