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
import org.nikitakapustkin.application.ports.in.commands.RemoveFriendCommand;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.FriendRemovedEventData;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.nikitakapustkin.domain.models.User;

@ExtendWith(MockitoExtension.class)
class RemoveFriendServiceTest {

  @Mock LoadUserPort loadUserPort;
  @Mock UpdateFriendsPort updateFriendsPort;
  @Mock PublishUserEventPort publishUserEventPort;

  @InjectMocks RemoveFriendService service;

  @Captor ArgumentCaptor<DomainEvent> eventCaptor;

  @Test
  void remove_friend_publishes_event() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    User user = User.builder().id(userId).login("alice").build();
    User friend = User.builder().id(friendId).login("bob").build();

    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.of(user));
    when(loadUserPort.loadUserById(friendId)).thenReturn(Optional.of(friend));

    service.removeFriend(new RemoveFriendCommand(userId, friendId));

    verify(updateFriendsPort).removeFriend(userId, friendId);
    verify(publishUserEventPort).publishUserEvent(eventCaptor.capture());

    DomainEvent event = eventCaptor.getValue();
    assertThat(event.getEntityId()).isEqualTo(userId);
    assertThat(event.getCorrelationId()).isEqualTo(friendId);
    assertThat(event.getEventType()).isEqualTo(EventType.FRIEND_REMOVED);
    assertThat(event.getEventTime()).isNotNull();
    assertThat(event.getEventDescription())
        .contains(userId.toString())
        .contains(friendId.toString());
    assertThat(event.getPayload()).isInstanceOf(FriendRemovedEventData.class);
    FriendRemovedEventData payload = (FriendRemovedEventData) event.getPayload();
    assertThat(payload.userId()).isEqualTo(userId);
    assertThat(payload.friendId()).isEqualTo(friendId);
    assertThat(payload.description()).contains(userId.toString()).contains(friendId.toString());
  }

  @Test
  void remove_friend_throws_when_user_missing() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.removeFriend(new RemoveFriendCommand(userId, friendId)))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(updateFriendsPort);
    verifyNoInteractions(publishUserEventPort);
  }

  @Test
  void remove_friend_throws_when_friend_missing() {
    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    when(loadUserPort.loadUserById(userId))
        .thenReturn(Optional.of(User.builder().id(userId).build()));
    when(loadUserPort.loadUserById(friendId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.removeFriend(new RemoveFriendCommand(userId, friendId)))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(updateFriendsPort);
    verifyNoInteractions(publishUserEventPort);
  }
}
