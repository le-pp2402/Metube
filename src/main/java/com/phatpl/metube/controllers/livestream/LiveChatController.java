package com.phatpl.metube.controllers.livestream;

import com.phatpl.metube.dtos.request.livestream.ChatMessageRequest;
import com.phatpl.metube.dtos.response.ChatMessageResponse;
import com.phatpl.metube.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/chat")
public class LiveChatController {

    private SimpMessagingTemplate messageTemplate;
    private UserService userService;

    @Autowired
    public LiveChatController(SimpMessagingTemplate messageTemplate, UserService userService) {
        this.messageTemplate = messageTemplate;
        this.userService = userService;
    }

    @PostMapping("/{channelId}")
    public ResponseEntity<?> postLiveChat(@PathVariable Integer channelId, @Valid @RequestBody ChatMessageRequest chatMessageRequest) {
        var username = userService.me().getUsername();
        ChatMessageResponse response = new ChatMessageResponse(
                username, chatMessageRequest.getMessage()
        );
        messageTemplate.convertAndSend("/channel/" + channelId, response);
        return ResponseEntity.ok(response);
    }
}
