package com.phatpl.metube.configs;

import com.phatpl.metube.services.video.RabbitMQUploadService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RabbitMQServiceStarter {

    @Autowired
    private RabbitMQUploadService rabbitMQUploadService;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "RabbitMQ-Upload-Service");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void startServices() {
        log.info("Starting RabbitMQ Upload Service...");
        executorService.submit(() -> {
            try {
                rabbitMQUploadService.run();
            } catch (Exception e) {
                log.error("Error in RabbitMQ Upload Service", e);
            }
        });
        log.info("RabbitMQ Upload Service submitted to executor");
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down RabbitMQ services...");

        if (rabbitMQUploadService != null) {
            rabbitMQUploadService.stop();
        }

        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Executor didn't terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for executor termination");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("RabbitMQ services shutdown completed");
    }
}