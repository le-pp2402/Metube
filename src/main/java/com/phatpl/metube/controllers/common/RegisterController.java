package com.phatpl.metube.controllers.common;

import com.phatpl.metube.dtos.request.identity.RegisterRequest;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.utils.BuildResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;

    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            return BuildResponse.badRequest(errors.get(0).getDefaultMessage());
        } else {
            try {
                var obj = userService.register(request);
                return BuildResponse.created(obj);
            } catch (Exception e) {
                return BuildResponse.badRequest(e.getMessage());
            }
        }
    }
}
