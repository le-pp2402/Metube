package com.phatpl.metube.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phatpl.metube.auth.dto.request.RegisterRequest;
import com.phatpl.metube.auth.service.AuthService;
import com.phatpl.metube.common.annotation.ValidateSchema;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterController {

  private final AuthService authService;

  public RegisterController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(
      @RequestBody @ValidateSchema("schemas/register.yaml") RegisterRequest registerRequest)
      throws JsonProcessingException {
    authService.register(registerRequest);
    return ResponseEntity.ok("success");
  }
}
