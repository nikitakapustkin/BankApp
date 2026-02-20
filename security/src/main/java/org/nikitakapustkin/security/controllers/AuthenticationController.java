package org.nikitakapustkin.security.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.adapters.in.web.security.TokenBlacklistService;
import org.nikitakapustkin.security.adapters.out.jwt.JwtService;
import org.nikitakapustkin.security.application.AuthenticationService;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.dto.LoginRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final JwtService jwtService;
  private final TokenBlacklistService tokenBlacklistService;

  @PostMapping(SecurityApiPaths.LOGIN)
  public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequest) {
    return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
  }

  @PostMapping(SecurityApiPaths.LOGOUT)
  public ResponseEntity<String> logout(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring(7);
      if (jwtService.validateToken(token)) {
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token).toInstant());
      }
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok("Logged out successfully");
  }
}
