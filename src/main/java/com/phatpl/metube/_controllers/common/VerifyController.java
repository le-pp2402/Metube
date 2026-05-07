package com.phatpl.metube._controllers.common;

import com.phatpl.metube._dtos.request.identity.VerifyEmailRequest;
import com.phatpl.metube._services.UserService;
import com.phatpl.metube._utils.BuildResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify")
public class VerifyController {

    private final UserService userService;

    @Autowired
    public VerifyController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    public ResponseEntity<?> verify(@RequestBody VerifyEmailRequest request) {
        try {
            return BuildResponse.ok(
                    userService.activeUser(request.getMail(), request.getCode())
            );
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

}
