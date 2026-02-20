package org.nikitakapustkin.security.adapters.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.security.adapters.out.jwt.JwtService;
import org.nikitakapustkin.security.models.JwtPrincipal;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String header = request.getHeader("Authorization");
      String token = null;
      String username = null;
      if (header != null && header.startsWith("Bearer ")) {
        token = header.substring(7);
        username = jwtService.extractUsername(token);
      }

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (!tokenBlacklistService.isRevoked(token) && jwtService.validateToken(token)) {
          UUID userId = jwtService.extractUserId(token);
          String role = jwtService.extractRole(token);
          UsernamePasswordAuthenticationToken authentication =
              getUsernamePasswordAuthenticationToken(role, username, userId);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception ex) {
      log.debug(
          "JWT validation failed for {} {}", request.getMethod(), request.getRequestURI(), ex);
    }
    filterChain.doFilter(request, response);
  }

  private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
      String role, String username, UUID userId) {
    List<GrantedAuthority> authorities =
        role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
    JwtPrincipal principal = new JwtPrincipal(username, userId, role);
    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }
}
