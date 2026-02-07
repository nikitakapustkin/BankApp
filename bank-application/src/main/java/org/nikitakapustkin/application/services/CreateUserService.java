package org.nikitakapustkin.application.services;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.CreateUserUseCase;
import org.nikitakapustkin.application.ports.in.commands.CreateUserCommand;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.UserCreatedEventData;
import org.nikitakapustkin.domain.exceptions.UserAlreadyExistsException;
import org.nikitakapustkin.domain.models.User;

@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final LoadUserPort loadUserPort;
    private final CreateUserPort createUserPort;
    private final PublishUserEventPort publishUserEventPort;

    @Override
    public User createUser(CreateUserCommand cmd) {
        if (loadUserPort.loadUserByLogin(cmd.getLogin()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists: " + cmd.getLogin());
        }

        var user = User.builder()
                .login(cmd.getLogin())
                .name(cmd.getName())
                .age(cmd.getAge())
                .sex(cmd.getSex())
                .hairColor(cmd.getHairColor())
                .build();

        var created = createUserPort.create(user);
        String description = "User created: " + created.getLogin();
        publishUserEventPort.publishUserEvent(DomainEvent.now(
                created.getId(),
                EventType.USER_CREATED,
                description,
                null,
                new UserCreatedEventData(
                        created.getId(),
                        created.getLogin(),
                        created.getName(),
                        created.getAge(),
                        created.getSex(),
                        created.getHairColor(),
                        description
                )
        ));
        return created;
    }
}
