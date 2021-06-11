package de.caritas.cob.liveservice.api.facade;

import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.service.LiveEventSendService;
import de.caritas.cob.liveservice.websocket.service.WebSocketSessionIdResolver;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LiveEventFacadeTest {

  @InjectMocks
  private LiveEventFacade liveEventFacade;

  @Mock
  private WebSocketSessionIdResolver sessionIdResolver;

  @Mock
  private LiveEventSendService liveEventSendService;

  @Test
  void triggerLiveEvent_Should_throwBadRequestException_When_liveEventMessageIsNull() {
    assertThrows(ResponseStatusException.class, () -> this.liveEventFacade.triggerLiveEvent(null));
  }

  @Test
  void triggerLiveEvent_Should_throwBadRequestException_When_eventTypeIsNull() {
    var liveEventMessage = new LiveEventMessage().eventType(null);
    assertThrows(ResponseStatusException.class,
        () -> this.liveEventFacade.triggerLiveEvent(liveEventMessage));
  }

  @Test
  void triggerLiveEvent_Should_callIdResolverWithIds_When_eventTypeIsValid() {
    List<String> expectedIds = asList("1", "2", "3");

    this.liveEventFacade.triggerLiveEvent(new LiveEventMessage()
        .eventType(DIRECTMESSAGE)
        .userIds(expectedIds));

    verify(this.sessionIdResolver, times(1)).resolveUserSessions(expectedIds);
  }

  @Test
  void triggerLiveEvent_Should_callsendLiveEventToUsers_When_parametersAreValid() {
    List<String> expectedIds = asList("1", "2", "3");
    List<WebSocketUserSession> socketUserSessions = expectedIds.stream()
        .map(id -> WebSocketUserSession.builder().userId(id).build())
        .collect(Collectors.toList());
    when(this.sessionIdResolver.resolveUserSessions(any())).thenReturn(socketUserSessions);

    this.liveEventFacade.triggerLiveEvent(new LiveEventMessage()
        .eventType(DIRECTMESSAGE)
        .userIds(expectedIds));

    verify(this.liveEventSendService, times(1))
        .sendLiveEventToUsers(eq(socketUserSessions),
            eq(new LiveEventMessage().eventType(DIRECTMESSAGE).userIds(expectedIds)));
  }

}
