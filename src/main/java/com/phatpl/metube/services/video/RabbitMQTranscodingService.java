package com.phatpl.metube.services.video;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class RabbitMQTranscodingService {


    ConnectionFactory connectionFactory;

    @Autowired
    public RabbitMQTranscodingService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void SendMessage(Integer id, String queueName) {

        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            /*
                queue: name of queue
                durable: true if we are declaring a durable queue (the queue will survive a server restart)
                exclusive: true if we are declaring an exclusive queue (restricted to this connection)
                autoDelete: true if we are declaring an autodelete queue (server will delete it when no longer in use)
                arguments: other properties (construction arguments) for the queue
             */
            channel.queueDeclare(queueName, true, false, false, null);
            String message = String.valueOf(id);

            /*
                exchange: the exchange to publish the message to
                routingKey: the routing key
                props: other properties for the message - routing headers etc
                body: the message body
             */
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            log.info("Producer sent message to queue: {}", message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
