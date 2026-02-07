package org.nikitakapustkin.adapters.in.web.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.in.web.dto.mapper.AccountWebMapper;
import org.nikitakapustkin.adapters.in.web.dto.mapper.UserWebMapper;
import org.nikitakapustkin.adapters.in.web.dto.response.CommonErrorResponses;
import org.nikitakapustkin.bank.contracts.dto.request.FriendRequestDto;
import org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto;
import org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto;
import org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto;
import org.nikitakapustkin.application.ports.in.AddFriendUseCase;
import org.nikitakapustkin.application.ports.in.DeleteUserUseCase;
import org.nikitakapustkin.application.ports.in.RemoveFriendUseCase;
import org.nikitakapustkin.application.ports.in.queries.GetAccountsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUserDetailsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUserFriendsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetUsersQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@CommonErrorResponses
@RestController
@Validated
@RequestMapping("/users")
@Tag(name = "User Controller", description = "API for managing users, friends, and accounts")
@RequiredArgsConstructor
public class UserController {

    private final DeleteUserUseCase deleteUserUseCase;
    private final AddFriendUseCase addFriendUseCase;
    private final RemoveFriendUseCase removeFriendUseCase;

    private final GetUsersQuery getUsersQuery;
    private final GetUserDetailsQuery getUserDetailsQuery;
    private final GetUserFriendsQuery getUserFriendsQuery;
    private final GetAccountsQuery getUserAccountsQuery;

    private final UserWebMapper userWebMapper;
    private final AccountWebMapper accountWebMapper;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(required = false)
            @Pattern(
                    regexp = "(?i)BLONDE|BLACK|RED|BROWN|WHITE|GRAY",
                    message = "Hair color must be BLONDE, BLACK, RED, BROWN, WHITE or GRAY"
            ) String hairColor,
            @RequestParam(required = false)
            @Pattern(regexp = "(?i)MALE|FEMALE", message = "Sex must be MALE or FEMALE") String sex) {

        var users = getUsersQuery.getUsers(hairColor, sex);
        var response = users.stream()
                .map(userWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsResponseDto> getUserInfo(@PathVariable("userId") UUID userId) {
        var userDetails = getUserDetailsQuery.getUserDetails(userId);

        var userResponse = userWebMapper.toResponse(userDetails.user(), userDetails.friendsLogins());
        var accountResponses = userDetails.accounts().stream()
                .map(accountWebMapper::toResponse)
                .toList();

        var details = new UserDetailsResponseDto(userResponse, userDetails.friendsLogins(), accountResponses);
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/friends")
    @Transactional
    public ResponseEntity<Void> addFriend(@Valid @RequestBody FriendRequestDto request) {
        var command = userWebMapper.toAddFriendCommand(request);
        addFriendUseCase.addFriend(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/friends")
    @Transactional
    public ResponseEntity<Void> removeFriend(@Valid @RequestBody FriendRequestDto request) {
        var command = userWebMapper.toRemoveFriendCommand(request);
        removeFriendUseCase.removeFriend(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserResponseDto>> getFriends(@PathVariable("userId") UUID userId) {
        var friends = getUserFriendsQuery.getFriends(userId);
        var response = friends.stream()
                .map(userWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/accounts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AccountResponseDto>> getAccounts(@PathVariable("userId") UUID userId) {
        var accounts = getUserAccountsQuery.getAccounts(userId);
        var response = accounts.stream()
                .map(accountWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
