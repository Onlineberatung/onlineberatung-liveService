package de.caritas.cob.liveservice.websocket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueuedLiveEventSendServiceTest {

  @InjectMocks
  QueuedLiveEventSendService queuedLiveEventSendService;

  @Mock
  LiveEventSendService liveEventSendService;

  @Mock
  LiveEventMessageQueue liveEventMessageQueue;

  @Mock
  WebSocketSessionIdResolver webSocketSessionIdResolver;

  @BeforeEach
  void setup() {
    setField(queuedLiveEventSendService, "maximumRetryAmount", 5);
  }

  @Test
  void sendQueuedLiveEvents_Should_userOnlyMessageQueue_When_noMessageIsQueued() {
    this.queuedLiveEventSendService.sendQueuedLiveEvents();

    verify(this.liveEventMessageQueue, times(1)).getCurrentOpenMessages();
    verifyNoMoreInteractions(this.liveEventSendService, this.webSocketSessionIdResolver);
  }

  @Test
  void sendQueuedLiveEvents_Should_removeMessageFromQueue_When_maximumRetryAmountIsReached() {
    when(this.liveEventMessageQueue.getCurrentOpenMessages()).thenReturn(Set.of(
        IdentifiedMessage.builder().retryAmount(10).messageId("messageid").build()));

    this.queuedLiveEventSendService.sendQueuedLiveEvents();

    verify(this.liveEventMessageQueue, times(1)).removeIdentifiedMessageWithId("messageid");
    verifyNoMoreInteractions(this.liveEventSendService, this.webSocketSessionIdResolver);
  }

  @Test
  void sendQueuedLiveEvents_Should_revalidateSessionAndSendMessage_When_messageIsQueued() {
    var socketSession = WebSocketUserSession.builder()
        .websocketSessionId("1")
        .userId("userid")
        .build();
    when(this.webSocketSessionIdResolver.resolveUserSession(anyString()))
        .thenReturn(Optional.of(socketSession));
    var message = IdentifiedMessage.builder()
        .retryAmount(1)
        .messageId("messageid")
        .websocketUserSession(socketSession)
        .build();
    when(this.liveEventMessageQueue.getCurrentOpenMessages()).thenReturn(Set.of(message));

    this.queuedLiveEventSendService.sendQueuedLiveEvents();

    verify(this.webSocketSessionIdResolver, times(1)).resolveUserSession("userid");
    verify(this.liveEventSendService, times(1)).sendIdentifiedMessage(any());
    assertThat(message.getRetryAmount(), is(2));
  }

}
