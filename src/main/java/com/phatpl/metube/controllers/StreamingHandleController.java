package com.phatpl.metube.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class StreamingHandleController {

//    @Autowired
//    private SimpMessagingTemplate simpMessagingTemplate;


    private final ConcurrentHashMap<Long, AtomicInteger> viewCount = new ConcurrentHashMap<>();

    @MessageMapping("/updateView.{videoId}")
    public void updateView(@DestinationVariable Long videoId, @Payload String message) {
        int newViewCount = viewCount.computeIfAbsent(videoId, k -> new AtomicInteger(0)).incrementAndGet();
//        simpMessagingTemplate.convertAndSend("/topic/videoViews." + videoId, newViewCount);
    }

    @MessageMapping("/news")
    @SendTo("/topic/news")
    public String broadcastNews(@Payload String message) {
      return message;
    }
}
