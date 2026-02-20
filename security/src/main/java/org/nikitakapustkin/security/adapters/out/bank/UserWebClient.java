package org.nikitakapustkin.security.adapters.out.bank;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.bank.contracts.dto.request.CreateUserRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.FriendRequestDto;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;
import org.nikitakapustkin.security.application.ports.out.UserBankClientPort;
import org.nikitakapustkin.security.constants.BankApiPaths;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.nikitakapustkin.security.dto.UserResponseDto;
import org.nikitakapustkin.security.mappers.BankResponseMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserWebClient implements UserBankClientPort {
  private final WebClient webClient;
  private final BankWebClientSupport support;

  public void sendUserCreateRequest(UserCreateRequestDto userCreateDto) {
    Sex sex = toContractSex(userCreateDto.getSex());
    HairColor hairColor = toContractHairColor(userCreateDto.getHairColor());
    CreateUserRequestDto request =
        new CreateUserRequestDto(
            userCreateDto.getLogin(),
            userCreateDto.getName(),
            userCreateDto.getAge(),
            sex,
            hairColor);
    webClient
        .post()
        .uri(BankApiPaths.USERS)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, support::toBankException)
        .toEntity(UUID.class)
        .block();
  }

  @Override
  public List<UserResponseDto> getUsers(String hairColor, String sex) {
    Mono<List<org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto>> response =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(BankApiPaths.USERS)
                        .queryParamIfPresent("hairColor", Optional.ofNullable(hairColor))
                        .queryParamIfPresent("sex", Optional.ofNullable(sex))
                        .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(new ParameterizedTypeReference<>() {});
    List<org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto> users =
        response.retryWhen(support.retrySpec()).block();
    if (users == null) {
      return List.of();
    }
    return users.stream().map(BankResponseMapper::toUserResponseDto).toList();
  }

  @Override
  public UserResponseDto getUserInfo(UUID userId) {
    Mono<org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto> response =
        webClient
            .get()
            .uri(BankApiPaths.USER_BY_ID, userId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(
                org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto.class);
    org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto details =
        response.retryWhen(support.retrySpec()).block();
    return BankResponseMapper.toUserResponseDto(details);
  }

  @Override
  public void createFriendship(UUID ownerId, UUID friendId) {
    webClient
        .post()
        .uri(BankApiPaths.USERS_FRIENDS)
        .bodyValue(new FriendRequestDto(ownerId, friendId))
        .retrieve()
        .onStatus(HttpStatusCode::isError, support::toBankException)
        .toEntity(Void.class)
        .block();
  }

  @Override
  public void removeFriendship(UUID ownerId, UUID friendId) {
    webClient
        .method(HttpMethod.DELETE)
        .uri(BankApiPaths.USERS_FRIENDS)
        .bodyValue(new FriendRequestDto(ownerId, friendId))
        .retrieve()
        .onStatus(HttpStatusCode::isError, support::toBankException)
        .toEntity(Void.class)
        .block();
  }

  @Override
  public void deleteUser(UUID userId) {
    Mono<Void> response =
        webClient
            .delete()
            .uri(BankApiPaths.USER_BY_ID, userId)
            .exchangeToMono(
                clientResponse -> {
                  int status = clientResponse.statusCode().value();
                  if (clientResponse.statusCode().is2xxSuccessful() || status == 404) {
                    return clientResponse.releaseBody();
                  }
                  return support.toBankException(clientResponse).flatMap(Mono::error);
                });
    support.maybeRetry(response, true).block();
  }

  private static Sex toContractSex(String sex) {
    if (sex == null || sex.isBlank()) {
      return null;
    }
    return Sex.valueOf(sex.toUpperCase(Locale.ROOT));
  }

  private static HairColor toContractHairColor(String hairColor) {
    if (hairColor == null || hairColor.isBlank()) {
      return null;
    }
    return HairColor.valueOf(hairColor.toUpperCase(Locale.ROOT));
  }
}
