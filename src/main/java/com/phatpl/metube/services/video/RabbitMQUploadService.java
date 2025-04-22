package com.phatpl.metube.services.video;

import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.utils.Constant;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class RabbitMQUploadService implements Runnable {

    @Value("${QUEUE_EVENT_UPLOAD}")
    private String QUEUE_EVENT_UPLOAD;

    private ConnectionFactory connectionFactory;
    private RabbitMQTranscodingService rabbitMQTranscodingService;
    private ResourceRepository resourceRepository;

    @Autowired
    public RabbitMQUploadService(ConnectionFactory connectionFactory, RabbitMQTranscodingService rabbitMQTranscodingService, ResourceRepository resourceRepository) {
        this.connectionFactory = connectionFactory;
        this.rabbitMQTranscodingService = rabbitMQTranscodingService;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run() {
        try {
            log.info("Starting listen on queue {}", QUEUE_EVENT_UPLOAD);

            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_EVENT_UPLOAD, true, false, false, null);

            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                log.info("Consumer received: {}", message);
                try {
                    processMessage(message);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            CancelCallback cancelCallback = consumerTag -> { };
            boolean autoAck = false;
            channel.basicConsume(QUEUE_EVENT_UPLOAD, autoAck, deliverCallback, cancelCallback);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void processMessage(String message) throws Exception {
        var messageHandler = new RabbitMQResponseHandler(message);
        String path = messageHandler.getKey();
        var resource = resourceRepository.findByVideo(path);
        if (resource.isPresent()) {
            rabbitMQTranscodingService.SendMessage(resource.get().getId(), Constant.VIDEO_TRANSCODING_QUEUE);
        } else {
            log.info("Resource not found: {}", path);
        }
    }
}
