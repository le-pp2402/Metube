package com.phatpl.metube.services.video;

import com.phatpl.metube.models.enums.ResourceStatus;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.utils.Constant;
import com.rabbitmq.client.*;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class RabbitMQUploadService implements Runnable {

    @Value("${QUEUE_EVENT_UPLOAD}")
    private String QUEUE_EVENT_UPLOAD;

    private final ConnectionFactory connectionFactory;
    private final RabbitMQTranscodingService rabbitMQTranscodingService;
    private final ResourceRepository resourceRepository;

    private Connection connection;
    private Channel channel;
    private volatile boolean isRunning = false;

    @Autowired
    public RabbitMQUploadService(
            ConnectionFactory connectionFactory,
            RabbitMQTranscodingService rabbitMQTranscodingService,
            ResourceRepository resourceRepository
    ) {
        this.connectionFactory = connectionFactory;
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setTopologyRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(5000);
        this.rabbitMQTranscodingService = rabbitMQTranscodingService;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run() {
        log.info("Starting to listen on queue {}", QUEUE_EVENT_UPLOAD);
        isRunning = true;

        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(QUEUE_EVENT_UPLOAD, true, false, false, null);

            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                long deliveryTag = delivery.getEnvelope().getDeliveryTag();

                try {
                    log.info("Processing message: {}", message);
                    processMessage(message);
                    channel.basicAck(deliveryTag, false);
                    log.info("Message processed successfully");
                } catch (Exception e) {
                    log.error("Error processing message: {}", message, e);
                    handleMessageError(message, deliveryTag, e);
                }
            };

            CancelCallback cancelCallback = consumerTag -> log.warn("Consumer was cancelled: {}", consumerTag);

            // Bắt đầu consume messages
            channel.basicConsume(QUEUE_EVENT_UPLOAD, false, deliverCallback, cancelCallback);

            log.info("Successfully started listening on queue: {}", QUEUE_EVENT_UPLOAD);

            // Keep the thread alive
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("Thread interrupted, stopping consumer");
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        } catch (IOException | TimeoutException e) {
            log.error("Error setting up RabbitMQ consumer", e);
        } finally {
            closeConnections();
        }
    }

    public void processMessage(String message) throws Exception {
        log.info("Message received: {}", message);

        var handler = new RabbitMQResponseHandler(message);
        String path = handler.getKey();
        var resource = resourceRepository
                .findByVideo(path.substring(path.indexOf('/') + 1))
                .orElse(null);

        if (resource != null) {
            resource.setStatus(ResourceStatus.WAITING);
            resourceRepository.save(resource);
            rabbitMQTranscodingService.SendMessage(
                    resource.getId(), Constant.VIDEO_TRANSCODING_QUEUE
            );
            log.info("Sent to queue {}: id={}, title={}",
                    Constant.VIDEO_TRANSCODING_QUEUE,
                    resource.getId(),
                    resource.getTitle()
            );
        } else {
            log.info("Resource {} skipped", message);
        }
    }

    private void handleMessageError(String message, long deliveryTag, Exception e) {
        try {
            if (shouldRequeue(e)) {
                log.info("Requeuing message due to recoverable error: {}", e.getMessage());
                channel.basicReject(deliveryTag, true);
            } else {
                log.error("Discarding message due to non-recoverable error: {}", e.getMessage());
                channel.basicReject(deliveryTag, false);
            }
        } catch (IOException ioException) {
            log.error("Error handling message rejection", ioException);
        }
    }

    private boolean shouldRequeue(Exception e) {
        // Requeue cho các lỗi có thể recover được
        if (e instanceof java.sql.SQLException ||
                e instanceof org.springframework.dao.DataAccessException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.util.concurrent.TimeoutException) {
            return true;
        }

        return !(e instanceof IllegalArgumentException) && !(e instanceof NullPointerException);
    }

    public void stop() {
        log.info("Stopping RabbitMQ Upload Service");
        isRunning = false;
    }

    @PreDestroy
    private void closeConnections() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            log.info("RabbitMQ connections closed");
        } catch (IOException | TimeoutException e) {
            log.error("Error closing RabbitMQ connections", e);
        }
    }
}