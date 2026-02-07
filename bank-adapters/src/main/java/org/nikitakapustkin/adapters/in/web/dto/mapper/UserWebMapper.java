package org.nikitakapustkin.adapters.in.web.dto.mapper;

import org.nikitakapustkin.bank.contracts.dto.request.CreateUserRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.FriendRequestDto;
import org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto;
import org.nikitakapustkin.application.ports.in.commands.CreateUserCommand;
import org.nikitakapustkin.application.ports.in.commands.AddFriendCommand;
import org.nikitakapustkin.application.ports.in.commands.RemoveFriendCommand;
import org.nikitakapustkin.domain.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserWebMapper {

    public CreateUserCommand toCreateCommand(CreateUserRequestDto dto) {
        return new CreateUserCommand(
                dto.login(),
                dto.name(),
                dto.age(),
                toDomainSex(dto.sex()),
                toDomainHairColor(dto.hairColor())
        );
    }

    public AddFriendCommand toAddFriendCommand(FriendRequestDto dto) {
        return new AddFriendCommand(dto.userId(), dto.friendId());
    }

    public RemoveFriendCommand toRemoveFriendCommand(FriendRequestDto dto) {
        return new RemoveFriendCommand(dto.userId(), dto.friendId());
    }

    public UserResponseDto toResponse(User user, List<String> friendsLogins) {
        return new UserResponseDto(
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getAge(),
                toContractSex(user.getSex()),
                toContractHairColor(user.getHairColor()),
                friendsLogins
        );
    }

    public UserResponseDto toResponse(User user) {
        return toResponse(user, List.of());
    }

    private static org.nikitakapustkin.domain.enums.Sex toDomainSex(
            org.nikitakapustkin.bank.contracts.enums.Sex sex) {
        if (sex == null) {
            return null;
        }
        return org.nikitakapustkin.domain.enums.Sex.valueOf(sex.name());
    }

    private static org.nikitakapustkin.domain.enums.HairColor toDomainHairColor(
            org.nikitakapustkin.bank.contracts.enums.HairColor hairColor) {
        if (hairColor == null) {
            return null;
        }
        return org.nikitakapustkin.domain.enums.HairColor.valueOf(hairColor.name());
    }

    private static org.nikitakapustkin.bank.contracts.enums.Sex toContractSex(
            org.nikitakapustkin.domain.enums.Sex sex) {
        if (sex == null) {
            return null;
        }
        return org.nikitakapustkin.bank.contracts.enums.Sex.valueOf(sex.name());
    }

    private static org.nikitakapustkin.bank.contracts.enums.HairColor toContractHairColor(
            org.nikitakapustkin.domain.enums.HairColor hairColor) {
        if (hairColor == null) {
            return null;
        }
        return org.nikitakapustkin.bank.contracts.enums.HairColor.valueOf(hairColor.name());
    }
}
