package com.phatpl.metube.controllers;

import com.phatpl.metube.dtos.request.stream.StartStreamRequest;
import com.phatpl.metube.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
            log.info("Stream key verified");
            HttpHeaders res = new HttpHeaders();
            res.set("Location", "PublicKeyLocation");
            return new ResponseEntity<>(res, HttpStatus.FOUND);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/on_segment/{streamkey}/{filepath}")
    public ResponseEntity<String> onSegment(@PathVariable("streamkey") String streamKey, @PathVariable("filepath") String filePath) {
        log.info("On segment request: {}", streamKey);
        log.info("On segment request: {}", filePath);
//        streamService.onNewSegment(streamKey, filePath);
        return ResponseEntity.ok().build();
    }
}
