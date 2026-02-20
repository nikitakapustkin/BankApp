package org.nikitakapustkin.security.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.UserService;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.dto.FriendRequestDto;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.nikitakapustkin.security.dto.UserResponseDto;
import org.nikitakapustkin.security.models.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping(SecurityApiPaths.USERS_REGISTER)
  @Transactional
  public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequestDto userCreateDto) {
    userService.createUser(userCreateDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping(SecurityApiPaths.USERS)
  public ResponseEntity<List<UserResponseDto>> getUsers(
      @RequestParam(required = false, name = "hairColor")
          @Pattern(
              regexp = "(?i)BLONDE|BLACK|RED|BROWN|WHITE|GRAY",
              message = "Hair color must be BLONDE, BLACK, RED, BROWN, WHITE or GRAY")
          String hairColor,
      @RequestParam(required = false, name = "sex")
          @Pattern(regexp = "(?i)MALE|FEMALE", message = "Sex must be MALE or FEMALE")
          String sex) {
    return ResponseEntity.ok(userService.getUsers(hairColor, sex));
  }

  @GetMapping(SecurityApiPaths.USERS_ME)
  public ResponseEntity<UserResponseDto> getUserInfo(Authentication authentication) {
    UUID userId = requireUserId(authentication);
    return ResponseEntity.ok(userService.getUserInfo(userId));
  }

  @GetMapping(SecurityApiPaths.USERS_ID)
  public ResponseEntity<UserResponseDto> getUser(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(userService.getUserInfo(userId));
  }

  @PostMapping(SecurityApiPaths.USERS_ME_FRIENDS)
  public ResponseEntity<Void> createFriends(
      @Valid @RequestBody FriendRequestDto friendRequest, Authentication authentication) {
    UUID ownerId = requireUserId(authentication);
    userService.createFriendship(ownerId, friendRequest.getFriendId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping(SecurityApiPaths.USERS_ME_FRIENDS)
  public ResponseEntity<Void> deleteFriends(
      @Valid @RequestBody FriendRequestDto friendRequest, Authentication authentication) {
    UUID ownerId = requireUserId(authentication);
    userService.deleteFriendship(ownerId, friendRequest.getFriendId());
    return ResponseEntity.noContent().build();
  }

  private static UUID requireUserId(Authentication authentication) {
    if (authentication == null) {
      throw new AccessDeniedException("Access denied");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof JwtPrincipal jwtPrincipal && jwtPrincipal.userId() != null) {
      return jwtPrincipal.userId();
    }
    throw new AccessDeniedException("Access denied");
  }
}
