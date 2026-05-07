package com.phatpl.metube._controllers.livestream;

import com.phatpl.metube._dtos.request.livestream.InititalizeStreamingRequest;
import com.phatpl.metube._services.livestream.LiveSessionService;
import com.phatpl.metube._utils.BuildResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        var liveSessions = liveSessionService.findAllDTO();
        return BuildResponse.ok(liveSessions);
    }

    @PostMapping
    public ResponseEntity<?> createLiveSession(@RequestBody InititalizeStreamingRequest request) {
        try {
            var res = liveSessionService.createLiveSession(request);
            return BuildResponse.ok(res);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
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
