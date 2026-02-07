package org.nikitakapustkin.application.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.application.ports.in.commands.CreateUserCommand;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.exceptions.UserAlreadyExistsException;
import org.nikitakapustkin.domain.models.User;
import org.nikitakapustkin.domain.events.payload.UserCreatedEventData;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock LoadUserPort loadUserPort;
    @Mock CreateUserPort createUserPort;
    @Mock PublishUserEventPort publishUserEventPort;

    @InjectMocks CreateUserService service;

    @Captor ArgumentCaptor<DomainEvent> eventCaptor;

    @Test
    void create_user_publishes_user_created_event() {
        CreateUserCommand cmd = new CreateUserCommand("alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK);
        when(loadUserPort.loadUserByLogin("alice")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();
        User created = User.builder()
                .id(userId)
                .login("alice")
                .name("Alice")
                .age(25)
                .sex(Sex.FEMALE)
                .hairColor(HairColor.BLACK)
                .build();
        when(createUserPort.create(any(User.class))).thenReturn(created);

        User result = service.createUser(cmd);

        assertThat(result).isEqualTo(created);
        verify(publishUserEventPort).publishUserEvent(eventCaptor.capture());

        DomainEvent event = eventCaptor.getValue();
        assertThat(event.getEntityId()).isEqualTo(userId);
        assertThat(event.getCorrelationId()).isNull();
        assertThat(event.getEventType()).isEqualTo(EventType.USER_CREATED);
        assertThat(event.getEventTime()).isNotNull();
        assertThat(event.getEventDescription()).contains("User created").contains("alice");
        assertThat(event.getPayload()).isInstanceOf(UserCreatedEventData.class);
        UserCreatedEventData payload = (UserCreatedEventData) event.getPayload();
        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.login()).isEqualTo("alice");
        assertThat(payload.name()).isEqualTo("Alice");
        assertThat(payload.age()).isEqualTo(25);
        assertThat(payload.sex()).isEqualTo(Sex.FEMALE);
        assertThat(payload.hairColor()).isEqualTo(HairColor.BLACK);
        assertThat(payload.description()).contains("User created").contains("alice");
    }

    @Test
    void create_user_throws_when_exists_and_does_not_publish() {
        CreateUserCommand cmd = new CreateUserCommand("alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK);
        when(loadUserPort.loadUserByLogin("alice")).thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.createUser(cmd))
                .isInstanceOf(UserAlreadyExistsException.class);

        verifyNoInteractions(createUserPort);
        verifyNoInteractions(publishUserEventPort);
    }
}
