package com.phatpl.metube.controllers.livestream;

import com.phatpl.metube.dtos.request.stream.ChatMessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class LiveChatController {

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @MessageMapping("/sendChat/{channelId}")
    public void sendChat(@DestinationVariable String channelId, @Payload ChatMessageRequest chat) {
        messageTemplate.convertAndSend("/channel/" + channelId, chat);
    }

    @MessageMapping("/joinChat/{channelId}")
    public void joinChat(@DestinationVariable String channelId, @Payload ChatMessageRequest chat, SimpMessageHeaderAccessor headerAccessor) {
        try {
            headerAccessor.getSessionAttributes().put("channelId", channelId);
            headerAccessor.getSessionAttributes().put("username", chat.getUsername());
            log.info("user {} joined chat channel {}", chat.getUsername(), channelId);
            messageTemplate.convertAndSend("/channel/" + channelId, chat.getUsername() + "joined living chat");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
