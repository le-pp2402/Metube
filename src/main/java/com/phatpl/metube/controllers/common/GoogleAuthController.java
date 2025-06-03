package com.phatpl.metube.controllers.common;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.phatpl.metube.dtos.request.identity.GoogleTokenRequest;
import com.phatpl.metube.mappers.LoginResponseMapper;
import com.phatpl.metube.services.identity.AuthService;
import com.phatpl.metube.services.identity.GoogleAuthService;
import com.phatpl.metube.services.identity.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GoogleAuthController {

    private final LoginResponseMapper loginResponseMapper;
    private GoogleAuthService googleAuthService;
    private JWTService jwtService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody GoogleTokenRequest request) {
        try {
            var userResponse = googleAuthService.verify(request.getGgToken());
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Invalid Google Token");
        }
    }
}
