package org.nikitakapustkin.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.application.ports.in.commands.AddFriendCommand;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.FriendAddedEventData;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.nikitakapustkin.domain.models.User;

@ExtendWith(MockitoExtension.class)
class AddFriendServiceTest {

  @Mock LoadUserPort loadUserPort;
  @Mock UpdateFriendsPort updateFriendsPort;
  @Mock PublishUserEventPort publishUserEventPort;

  @InjectMocks AddFriendService service;

  @Captor ArgumentCaptor<DomainEvent> eventCaptor;

  @Test
  void add_friend_publishes_event() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    User user = User.builder().id(userId).login("alice").build();
    User friend = User.builder().id(friendId).login("bob").build();

    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.of(user));
    when(loadUserPort.loadUserById(friendId)).thenReturn(Optional.of(friend));

    service.addFriend(new AddFriendCommand(userId, friendId));

    verify(updateFriendsPort).addFriend(userId, friendId);
    verify(publishUserEventPort).publishUserEvent(eventCaptor.capture());

    DomainEvent event = eventCaptor.getValue();
    assertThat(event.getEntityId()).isEqualTo(userId);
    assertThat(event.getCorrelationId()).isEqualTo(friendId);
    assertThat(event.getEventType()).isEqualTo(EventType.FRIEND_ADDED);
    assertThat(event.getEventTime()).isNotNull();
    assertThat(event.getEventDescription())
        .contains(userId.toString())
        .contains(friendId.toString());
    assertThat(event.getPayload()).isInstanceOf(FriendAddedEventData.class);
    FriendAddedEventData payload = (FriendAddedEventData) event.getPayload();
    assertThat(payload.userId()).isEqualTo(userId);
    assertThat(payload.friendId()).isEqualTo(friendId);
    assertThat(payload.description()).contains(userId.toString()).contains(friendId.toString());
  }

  @Test
  void add_friend_throws_when_user_missing() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addFriend(new AddFriendCommand(userId, friendId)))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(updateFriendsPort);
    verifyNoInteractions(publishUserEventPort);
  }

  @Test
  void add_friend_throws_when_friend_missing() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    when(loadUserPort.loadUserById(userId))
        .thenReturn(Optional.of(User.builder().id(userId).build()));
    when(loadUserPort.loadUserById(friendId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addFriend(new AddFriendCommand(userId, friendId)))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(updateFriendsPort);
    verifyNoInteractions(publishUserEventPort);
  }
}
