package com.phatpl.metube.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phatpl.metube.auth.dto.request.LoginRequest;
import com.phatpl.metube.common.annotation.ValidateSchema;
import com.phatpl.metube.common.api.ApiResponse;

import io.github.lepp2402.service.POWService;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

  private final POWService powService;

  public LoginController(POWService powService) {
    this.powService = powService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<Void>> login(
      @RequestBody @ValidateSchema("schemas/login.yaml") LoginRequest loginRequest) {
    return ResponseEntity.ok(ApiResponse.success());
  }

  @GetMapping("/pow/challenge")
  public ResponseEntity<ApiResponse<?>> getChallenge() {
    return ResponseEntity.ok(ApiResponse.success(powService.genChallenge(3, 10)));
  }
}
