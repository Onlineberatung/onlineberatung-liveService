package de.caritas.cob.liveservice.api.facade;

import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.service.LiveEventSendService;
import de.caritas.cob.liveservice.websocket.service.WebSocketSessionIdResolver;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class LiveEventFacadeTest {

  @InjectMocks
  private LiveEventFacade liveEventFacade;

  @Mock
  private WebSocketSessionIdResolver sessionIdResolver;

  @Mock
  private LiveEventSendService liveEventSendService;

  @Test(expected = ResponseStatusException.class)
  public void triggerLiveEvent_Should_throwBadRequestException_When_liveEventMessageIsNull() {
    this.liveEventFacade.triggerLiveEvent(null);
  }

  @Test(expected = ResponseStatusException.class)
  public void triggerLiveEvent_Should_throwBadRequestException_When_eventTypeIsNull() {
    this.liveEventFacade.triggerLiveEvent(new LiveEventMessage().eventType(null));
  }

  @Test
  public void triggerLiveEvent_Should_callIdResolverWithIds_When_eventTypeIsValid() {
    List<String> expectedIds = asList("1", "2", "3");

    this.liveEventFacade.triggerLiveEvent(new LiveEventMessage()
        .eventType(DIRECTMESSAGE)
        .userIds(expectedIds));

    verify(this.sessionIdResolver, times(1)).resolveUserIds(eq(expectedIds));
  }

  @Test
  public void triggerLiveEvent_Should_callsendLiveEventToUsers_When_parametersAreValid() {
    List<String> expectedIds = asList("1", "2", "3");
    when(this.sessionIdResolver.resolveUserIds(any())).thenReturn(expectedIds);

    this.liveEventFacade.triggerLiveEvent(new LiveEventMessage()
        .eventType(DIRECTMESSAGE)
        .userIds(expectedIds));

    verify(this.liveEventSendService, times(1))
        .sendLiveEventToUsers(eq(expectedIds),
            eq(new LiveEventMessage().eventType(DIRECTMESSAGE).userIds(expectedIds)));
  }

}
