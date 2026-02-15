package com.aniket.mirror.upload.controller;

import com.aniket.mirror.upload.dto.auth.AuthResponse;
import com.aniket.mirror.upload.dto.auth.LoginRequest;
import com.aniket.mirror.upload.dto.auth.RegisterRequest;
import com.aniket.mirror.upload.dto.auth.ResetPasswordRequest;
import com.aniket.mirror.upload.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(
      @RequestBody LoginRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }
  
  @PostMapping("/password-reset-init")
  public ResponseEntity<String> initPasswordReset(@RequestParam String email) {
      String token = service.forgotPassword(email);
      // In real world, we'd say "Check your email".
      // But for dev/demo, we return the token.
      return ResponseEntity.ok("Reset token (for demo): " + token);
  }
  
  @PostMapping("/password-reset-confirm")
  public ResponseEntity<String> confirmPasswordReset(@RequestBody ResetPasswordRequest req) {
      service.resetPassword(req.getToken(), req.getNewPassword());
      return ResponseEntity.ok("Password reset successfully. Please login.");
  }
}
