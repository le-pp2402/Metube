package com.phatpl.metube._controllers.common;


import com.phatpl.metube._dtos.request.identity.LoginRequest;
import com.phatpl.metube._services.identity.AuthService;
import com.phatpl.metube._utils.BuildResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/login")
public class LoginController {
    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return BuildResponse.ok(authService.login(loginRequest));
        } catch (RuntimeException e) {
            return BuildResponse.unauthorized(e.getMessage());
        }
    }
}
