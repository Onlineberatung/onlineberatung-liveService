package de.caritas.cob.liveservice.websocket.service;

import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class to send all queued live event messages which were not acknowleged by the stomp
 * client.
 */
@Service
@RequiredArgsConstructor
public class QueuedLiveEventSendService {

  @Value("${live.event.retry.amount}")
  private Integer maximumRetryAmount;

  private final @NonNull LiveEventSendService liveEventSendService;
  private final @NonNull LiveEventMessageQueue liveEventMessageQueue;
  private final @NonNull WebSocketSessionIdResolver webSocketSessionIdResolver;

  /**
   * Retries to send all queued live event messages which were not acknowleged by the stomp client.
   */
  public void sendQueuedLiveEvents() {
    this.liveEventMessageQueue.getCurrentOpenMessages()
        .forEach(this::sendLiveEventToUser);
  }

  private void sendLiveEventToUser(IdentifiedMessage identifiedMessage) {
    if (identifiedMessage.getRetryAmount() >= this.maximumRetryAmount) {
      this.liveEventMessageQueue.removeIdentifiedMessageWithId(identifiedMessage.getMessageId());
    } else {
      revalidateUsersWebsocketSession(identifiedMessage);
      this.liveEventSendService.sendIdentifiedMessage(identifiedMessage);
      incrementRetryAmount(identifiedMessage);
    }
  }

  private void revalidateUsersWebsocketSession(IdentifiedMessage identifiedMessage) {
    this.webSocketSessionIdResolver
        .resolveUserSession(identifiedMessage.getWebsocketUserSession().getUserId())
        .ifPresent(identifiedMessage::setWebsocketUserSession);
  }

  private void incrementRetryAmount(IdentifiedMessage identifiedMessage) {
    identifiedMessage.setRetryAmount(identifiedMessage.getRetryAmount() + 1);
  }

}
