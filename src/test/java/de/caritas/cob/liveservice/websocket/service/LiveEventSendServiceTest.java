package de.caritas.cob.liveservice.websocket.service;

import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static de.caritas.cob.liveservice.websocket.model.Subscription.EVENTS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.caritas.cob.liveservice.websocket.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.liveeventmessage.BasicLiveEventMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class LiveEventSendServiceTest {

  @InjectMocks
  private LiveEventSendService liveEventSendService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Test
  public void sendLiveEventToUsers_Should_notInteractWithMessagingTemplate_When_sessionIdsAreNull() {
    this.liveEventSendService.sendLiveEventToUsers(null, buildBasicLiveEventMessage());

    verifyNoInteractions(messagingTemplate);
  }

  @Test
  public void sendLiveEventToUsers_Should_notInteractWithMessagingTemplate_When_sessionIdsAreEmpty() {
    this.liveEventSendService.sendLiveEventToUsers(emptyList(), buildBasicLiveEventMessage());

    verifyNoInteractions(messagingTemplate);
  }

  @Test
  public void sendLiveEventToUsers_Should_sendEventMessageToExpectedUser_When_sessionIdIsGiven() {
    this.liveEventSendService.sendLiveEventToUsers(singletonList("1"), buildBasicLiveEventMessage());

    verify(messagingTemplate, times(1))
        .convertAndSendToUser(eq("1"), eq(EVENTS.getSubscriptionEndpoint()), eq(buildBasicLiveEventMessage()),
            any(MessageHeaders.class));
  }

  @Test
  public void sendLiveEventToUsers_Should_sendEventMessageToAllUsers_When_sessionIdsAreGiven() {
    this.liveEventSendService.sendLiveEventToUsers(asList("1", "2", "3", "4", "5"), buildBasicLiveEventMessage());

    verify(messagingTemplate, times(5))
        .convertAndSendToUser(anyString(), eq(EVENTS.getSubscriptionEndpoint()), eq(buildBasicLiveEventMessage()),
            any(MessageHeaders.class));
  }

  private BasicLiveEventMessage buildBasicLiveEventMessage() {
    return BasicLiveEventMessage.builder()
        .eventType(DIRECTMESSAGE.toString())
        .build();
  }

}
