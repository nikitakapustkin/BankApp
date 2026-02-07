package org.nikitakapustkin.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.application.ports.in.ImportUserUseCase;
import org.nikitakapustkin.application.ports.in.commands.ImportUserCommand;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.domain.models.User;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class ImportUserService implements ImportUserUseCase {

    private final LoadUserPort loadUserPort;
    private final CreateUserPort createUserPort;

    @Override
    public User importUser(ImportUserCommand cmd) {
        var existingById = loadUserPort.loadUserById(cmd.getUserId());
        if (existingById.isPresent()) {
            return existingById.get();
        }

        var existingByLogin = loadUserPort.loadUserByLogin(cmd.getLogin());
        if (existingByLogin.isPresent()) {
            UUID existingId = existingByLogin.get().getId();
            if (existingId != null && !existingId.equals(cmd.getUserId())) {
                log.info("User login {} already exists with id {}, skipping import {}", cmd.getLogin(), existingId, cmd.getUserId());
            }
            return existingByLogin.get();
        }

        var user = User.builder()
                .id(cmd.getUserId())
                .login(cmd.getLogin())
                .name(cmd.getName())
                .age(cmd.getAge())
                .sex(cmd.getSex())
                .hairColor(cmd.getHairColor())
                .build();

        return createUserPort.create(user);
    }
}
