package com.phatpl.metube.controllers.common;


import com.phatpl.metube.dtos.request.identity.LoginRequest;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.services.identity.AuthService;
import com.phatpl.metube.utils.BuildResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class LoginController {
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public LoginController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return BuildResponse.ok(
                authService.login(loginRequest)
        );
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> logout() {
//        JwtAuthenticationToken JwtAuthToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        SecurityContextHolder.getContext().
//        authService.logout(token);
//        return BuildResponse.ok("Logged out successfully");
//    }
}
