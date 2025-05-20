package com.phatpl.metube.services.video;

import com.phatpl.metube.models.enums.ResourceStatus;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.utils.Constant;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
    TODO: Test this function make sure it still alive after receiving a message and process each message one by one
 */
@Slf4j
@Service
public class RabbitMQUploadService implements Runnable {

    @Value("${QUEUE_EVENT_UPLOAD}")
    private String QUEUE_EVENT_UPLOAD;

    private final ConnectionFactory connectionFactory;
    private final RabbitMQTranscodingService rabbitMQTranscodingService;
    private final ResourceRepository resourceRepository;

    @Autowired
    public RabbitMQUploadService(
            ConnectionFactory connectionFactory,
            RabbitMQTranscodingService rabbitMQTranscodingService,
            ResourceRepository resourceRepository
    ) {
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setTopologyRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(5000);
        this.connectionFactory = connectionFactory;
        this.rabbitMQTranscodingService = rabbitMQTranscodingService;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run() {
        log.info("Starting listen on queue {}", QUEUE_EVENT_UPLOAD);
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
}
