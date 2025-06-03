package com.phatpl.metube.controllers.livestream;

import com.phatpl.metube.dtos.request.livestream.InititalizeStreamingRequest;
import com.phatpl.metube.services.livestream.LiveSessionService;
import com.phatpl.metube.utils.BuildResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/live-session")
public class LiveSessionController {
    public LiveSessionService liveSessionService;

    @Autowired
    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @GetMapping
    public ResponseEntity<?> getLiveSessions() {
        var liveSessions = liveSessionService.getAllAccessibleLiveSession();
        return BuildResponse.ok(liveSessions);
    }

    @PostMapping("/start")
    public ResponseEntity<?> createLiveSession(@RequestBody InititalizeStreamingRequest request) {
        try {
            var res = liveSessionService.createLiveSession(request);
            return BuildResponse.ok(res);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopLiveSession() {
        try {
            liveSessionService.stopLiveSession();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return BuildResponse.unauthorized(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLiveSession(@PathVariable Integer id) {
        try {
            var liveSession = liveSessionService.findDTOById(id);
            return BuildResponse.ok(liveSession);
        } catch (EntityNotFoundException e) {
            return BuildResponse.notFound(e.getMessage());
        }
    }
}
