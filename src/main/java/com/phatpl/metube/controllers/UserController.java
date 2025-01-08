package com.phatpl.metube.controllers;

import com.phatpl.metube.dtos.request.UpdatePasswordRequest;
import com.phatpl.metube.dtos.response.UserResponse;
import com.phatpl.metube.filters.UserFilter;
import com.phatpl.metube.models.User;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.utils.BuildResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends BaseController<User, UserResponse, UserFilter, Integer> {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UpdatePasswordRequest request) {
        try {
            return BuildResponse.ok(userService.updateUserInfo(
                    request.getOldPassword(), request.getNewPassword()
            ));
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        try {
            return BuildResponse.ok(userService.me());
        } catch (Exception e) {
            return BuildResponse.unauthorized(e.getMessage());
        }
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority(SCOPE_ADMIN)")
    public ResponseEntity<?> findAll(UserFilter userFilter) {
        var users = userService.findAllDTO();
        return BuildResponse.ok(users);
    }

    @PostMapping("/streamkey")
    public ResponseEntity<?> getStreamKey() {
        try {
            return BuildResponse.ok(userService.getStreamKey());
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}