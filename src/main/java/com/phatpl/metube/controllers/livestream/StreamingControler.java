package com.phatpl.metube.controllers.livestream;

import com.phatpl.metube.services.ResourceService;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.services.livestream.LiveSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/live")
public class StreamingControler {

    private final LiveSessionService liveSessionService;
    private UserService userService;
    private ResourceService resourceService;

    @Autowired
    public StreamingControler(UserService userService, ResourceService resourceService, LiveSessionService liveSessionService) {
        this.userService = userService;
        this.resourceService = resourceService;
        this.liveSessionService = liveSessionService;
    }

    @PostMapping
    public ResponseEntity<?> startStream(@RequestParam("name") String streamKey) {
        log.info("Start stream request: {}", streamKey);
        var user = userService.findByStreamKey(streamKey);
        if (user != null) {
            log.info("Stream key verified");
            HttpHeaders res = new HttpHeaders();
            res.set("Location", user.getUsername());
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

    @PutMapping
    public ResponseEntity<?> stopStream(@RequestParam("name") String streamKey) {
        log.info("Stop stream request: {}", streamKey);
        var user = userService.findByStreamKey(streamKey);
        if (user != null) {
            liveSessionService.deleteByUserId(user.getId());
        }
        return ResponseEntity.ok().build();
    }
}
