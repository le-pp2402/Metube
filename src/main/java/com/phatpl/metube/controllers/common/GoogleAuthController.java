package com.phatpl.metube.controllers.common;

import com.phatpl.metube.dtos.request.identity.GoogleTokenRequest;
import com.phatpl.metube.services.identity.GoogleAuthService;
import com.phatpl.metube.utils.BuildResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody GoogleTokenRequest request) {
        try {
            var userResponse = googleAuthService.verify(request.getGgToken());
            return BuildResponse.ok(userResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Invalid Google Token");
        }
    }
}
