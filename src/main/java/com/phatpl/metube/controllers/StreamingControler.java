package com.phatpl.metube.controllers;

import com.phatpl.metube.dtos.request.stream.StartStreamRequest;
import com.phatpl.metube.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/live")
public class StreamingControler {

    private UserService userService;

    @Autowired
    public StreamingControler(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> startStream(@RequestParam("name") String streamKey) {
        log.info("Start stream request: {}", streamKey);
        if (userService.verifyStreamKey(streamKey)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
