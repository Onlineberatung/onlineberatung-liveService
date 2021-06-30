package de.caritas.cob.liveservice.websocket.service;

import static de.caritas.cob.liveservice.websocket.model.Subscription.EVENTS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class LiveEventSendServiceTest {

  @InjectMocks
  private LiveEventSendService liveEventSendService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private LiveEventMessageQueue liveEventMessageQueue;

  @Test
  void sendLiveEventToUsers_Should_notInteractWithMessagingTemplate_When_sessionIdsAreNull() {
    this.liveEventSendService.sendLiveEventToUsers(null, buildLiveEventMessage());

    verifyNoInteractions(messagingTemplate);
  }

  @Test
  void sendLiveEventToUsers_Should_notInteractWithMessagingTemplate_When_sessionIdsAreEmpty() {
    this.liveEventSendService.sendLiveEventToUsers(emptyList(),
        buildLiveEventMessage());

    verifyNoInteractions(messagingTemplate);
  }

  @Test
  void sendLiveEventToUsers_Should_sendEventMessageToExpectedUser_When_sessionIdIsGiven() {
    this.liveEventSendService
        .sendLiveEventToUsers(
            singletonList(WebSocketUserSession.builder().websocketSessionId("1").build()),
            buildLiveEventMessage());

    verify(messagingTemplate, times(1))
        .convertAndSendToUser(eq("1"), eq(EVENTS.getSubscriptionEndpoint()), eq(
            buildLiveEventMessage()), any(MessageHeaders.class));
  }

  @Test
  void sendLiveEventToUsers_Should_sendEventMessageToAllUsers_When_sessionIdsAreGiven() {
    var userSession = Stream.of("1", "2", "3", "4", "5")
        .map(id -> WebSocketUserSession.builder().websocketSessionId(id).build())
        .collect(Collectors.toList());

    this.liveEventSendService.sendLiveEventToUsers(userSession, buildLiveEventMessage());

    verify(messagingTemplate, times(5))
        .convertAndSendToUser(anyString(), eq(EVENTS.getSubscriptionEndpoint()), eq(
            buildLiveEventMessage()), any(MessageHeaders.class));
  }

  private LiveEventMessage buildLiveEventMessage() {
    return new LiveEventMessage()
        .eventType(EventType.DIRECTMESSAGE)
        .userIds(null);
  }

}
