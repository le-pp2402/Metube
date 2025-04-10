package com.phatpl.metube.configs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    public void handleWebsocketDisconnectListener(SessionDisconnectEvent event) {
        // reduce number of user view
    }


}
