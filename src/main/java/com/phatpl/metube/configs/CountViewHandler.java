package com.phatpl.metube.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CountViewHandler implements org.springframework.web.socket.WebSocketHandler {


    private final ConcurrentHashMap<String, AtomicInteger> viewCount = new ConcurrentHashMap<String, AtomicInteger>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established on session: {}", session.getId());
        viewCount.computeIfAbsent("VIDEO_VIEW_COUNT", k -> new AtomicInteger(0)).incrementAndGet();
        session.sendMessage(new TextMessage("Completed processing game: " + viewCount.get("VIDEO_VIEW_COUNT")));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String gameId = (String) message.getPayload();
        session.sendMessage(new TextMessage("Started processing game: " + gameId));
        Thread.sleep(1000);
        session.sendMessage(new TextMessage("Completed processing game: " + gameId));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
         log.info("Exception occured: {} on session: {}", exception.getMessage(), session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
       viewCount.computeIfAbsent("VIDEO_VIEW_COUNT", k -> new AtomicInteger(0)).decrementAndGet();
       try {
           session.sendMessage(new TextMessage("Completed processing game: " + viewCount.get("VIDEO_VIEW_COUNT")));
       } catch (Exception e) {
//           log.error("Error occured: {}", e.getMessage());
       }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
