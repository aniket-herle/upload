package com.aniket.mirror.upload.service;

import com.aniket.mirror.upload.constants.Role;
import com.aniket.mirror.upload.dto.auth.AuthResponse;
import com.aniket.mirror.upload.dto.auth.LoginRequest;
import com.aniket.mirror.upload.dto.auth.RegisterRequest;
import com.aniket.mirror.upload.entity.User;
import com.aniket.mirror.upload.repository.UserRepository;
import com.aniket.mirror.upload.security.JwtService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();
    repository.save(user);
    var jwtToken = jwtService.generateToken(user);
    return AuthResponse.builder()
        .accessToken(jwtToken)
        .build();
  }

  public AuthResponse authenticate(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    return AuthResponse.builder()
        .accessToken(jwtToken)
        .build();
  }
  
  public String forgotPassword(String email) {
      var user = repository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));
      
      // In a real app, send actual email.
      // Here we just return the token for the user to copy-paste.
      String token = UUID.randomUUID().toString();
      user.setResetToken(token);
      repository.save(user);
      return token;
  }
  
  public void resetPassword(String token, String newPassword) {
      if (token == null || token.isBlank()) {
           throw new IllegalArgumentException("Invalid token");
      }
      // This is inefficient but functional for MVP. 
      // Ideally findByResetToken.
      var user = repository.findAll().stream()
          .filter(u -> token.equals(u.getResetToken()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
          
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setResetToken(null);
      repository.save(user);
  }
}
