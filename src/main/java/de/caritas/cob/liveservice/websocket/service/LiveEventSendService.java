package de.caritas.cob.liveservice.websocket.service;

import static de.caritas.cob.liveservice.websocket.model.Subscription.EVENTS;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

/**
 * Service to push live events to subscribed active socket session users.
 */
@Service
@RequiredArgsConstructor
public class LiveEventSendService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LiveEventSendService.class);
  private static final String NATIVE_HEADER_ID = "id";

  private final @NonNull SimpMessagingTemplate simpMessagingTemplate;
  private final @NonNull LiveEventMessageQueue liveEventMessageQueue;

  /**
   * Sends a live event with {@link EventType} to given socket sessions.
   *
   * @param socketUserSessions the session ids to send the event
   * @param liveEventMessage   the live event message object
   */
  public void sendLiveEventToUsers(List<WebSocketUserSession> socketUserSessions,
      LiveEventMessage liveEventMessage) {
    liveEventMessage.userIds(null);
    if (isNotEmpty(socketUserSessions)) {
      LOGGER.info("Send message with type {} to users with ids {}",
          liveEventMessage.getEventType(),
          socketUserSessions.stream().map(WebSocketUserSession::getUserId)
              .collect(Collectors.toList()));
      socketUserSessions.forEach(sessionId -> sendEventMessageToUser(liveEventMessage, sessionId));
    }
  }

  private void sendEventMessageToUser(LiveEventMessage liveEventMessage,
      WebSocketUserSession webSocketUserSession) {
    var messageId = randomUUID().toString();
    var identifiedMessage = IdentifiedMessage.builder()
        .messageId(messageId)
        .liveEventMessage(liveEventMessage)
        .websocketUserSession(webSocketUserSession)
        .retryAmount(1)
        .createdDate(LocalDateTime.now(ZoneOffset.UTC))
        .build();
    this.liveEventMessageQueue.addIdentifiedMessage(identifiedMessage);

    sendIdentifiedMessage(identifiedMessage);
  }

  public void sendIdentifiedMessage(IdentifiedMessage identifiedMessage) {
    var headerAccessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
    headerAccessor
        .setSessionId(identifiedMessage.getWebsocketUserSession().getWebsocketSessionId());
    headerAccessor.setMessageId(identifiedMessage.getMessageId());
    headerAccessor.addNativeHeader(NATIVE_HEADER_ID, identifiedMessage.getMessageId());
    headerAccessor.setLeaveMutable(true);
    this.simpMessagingTemplate
        .convertAndSendToUser(identifiedMessage.getWebsocketUserSession().getWebsocketSessionId(),
            EVENTS.getSubscriptionEndpoint(), identifiedMessage.getLiveEventMessage(),
            headerAccessor.getMessageHeaders());
  }

}
