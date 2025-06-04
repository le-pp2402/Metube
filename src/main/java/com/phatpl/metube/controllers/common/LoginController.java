package com.phatpl.metube.controllers.common;

import com.phatpl.metube.dtos.request.identity.LoginRequest;
import com.phatpl.metube.services.identity.AuthService;
import com.phatpl.metube.utils.BuildResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class LoginController {
    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return BuildResponse.ok(
                authService.login(loginRequest)
        );
    }
}
