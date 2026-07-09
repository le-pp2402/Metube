package com.phatpl.metube.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phatpl.metube.auth.dto.request.LoginRequest;
import com.phatpl.metube.common.annotation.ValidateSchema;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @ValidateSchema("schemas/login.yaml") LoginRequest loginRequest) {
    return ResponseEntity.ok("Login successful");
  }

  @GetMapping("/pow/challenge")
  public ResponseEntity<String> getChallenge() {
    // Generate a challenge for proof-of-work
    String challenge = "random-challenge-string"; // Replace with actual challenge generation logic
    return ResponseEntity.ok(challenge);
  }
}
