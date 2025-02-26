package com.phatpl.metube.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CountViewHandler implements org.springframework.web.socket.WebSocketHandler {

    private final ConcurrentHashMap<String, AtomicInteger> viewCount = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final String tempKey = "VIEW_COUNT";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established on session: {}", session.getId());
        sessions.add(session);

        int currentView = viewCount.computeIfAbsent(tempKey, k -> new AtomicInteger(0)).incrementAndGet();
        broadcastViewCount(currentView);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Xử lý thông điệp từ client nếu cần
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Exception occurred on session {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Connection closed on session: {}", session.getId());
        sessions.remove(session);

        int currentView = viewCount.computeIfAbsent(tempKey, k -> new AtomicInteger(0)).decrementAndGet();
        broadcastViewCount(currentView);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void broadcastViewCount(int viewCount) {
        TextMessage message = new TextMessage(String.valueOf(viewCount));
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }
}
