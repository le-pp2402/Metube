package com.phatpl.metube._controllers.livestream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phatpl.metube._services.UserService;
import com.phatpl.metube._services.livestream.LiveSessionService;
import com.phatpl.metube._services.video.ResourceService;

@Slf4j
@RestController
@RequestMapping("/live")
public class StreamingControler {

    private final LiveSessionService liveSessionService;
    private final UserService userService;

    @Autowired
    public StreamingControler(UserService userService, ResourceService resourceService, LiveSessionService liveSessionService) {
        this.userService = userService;
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
