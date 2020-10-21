package de.caritas.cob.liveservice.websocket.service;

import static de.caritas.cob.liveservice.websocket.model.Subscription.EVENTS;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.websocket.model.LiveEventMessage;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to push live events to subscribed active socket session users.
 */
@Service
@RequiredArgsConstructor
public class LiveEventSendService {

  private final @NonNull SimpMessagingTemplate simpMessagingTemplate;

  /**
   * Sends a live event with {@link EventType} to given socket sessions.
   *
   * @param socketSessionIds the session ids to send the event
   * @param eventType the event type
   */
  public void sendLiveEventToUsers(List<String> socketSessionIds, EventType eventType) {
    if (isNotEmpty(socketSessionIds)) {
      LiveEventMessage liveEventMessage = LiveEventMessage.builder()
          .eventType(eventType.toString())
          .build();
      socketSessionIds.forEach(sessionId -> sendEventMessageToUser(liveEventMessage, sessionId));
    }
  }

  private void sendEventMessageToUser(LiveEventMessage liveEventMessage, String sessionId) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
        .create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    headerAccessor.setLeaveMutable(true);
    this.simpMessagingTemplate
        .convertAndSendToUser(sessionId, EVENTS.getSubscriptionEndpoint(), liveEventMessage,
            headerAccessor.getMessageHeaders());
  }

}
