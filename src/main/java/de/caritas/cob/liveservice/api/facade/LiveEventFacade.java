package de.caritas.cob.liveservice.api.facade;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.service.LiveEventSendService;
import de.caritas.cob.liveservice.websocket.service.WebSocketSessionIdResolver;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Facade to encapsulate services and logic needed to trigger live events.
 */
@Service
@RequiredArgsConstructor
public class LiveEventFacade {

  private final @NonNull WebSocketSessionIdResolver sessionIdResolver;
  private final @NonNull LiveEventSendService liveEventSendService;

  /**
   * Triggers a live event to given registered users.
   *
   * @param liveEventMessage the type of the event message
   */
  public void triggerLiveEvent(LiveEventMessage liveEventMessage) {
    validateEventType(liveEventMessage);
    List<String> socketSessionIds = this.sessionIdResolver
        .resolveUserIds(liveEventMessage.getUserIds());
    this.liveEventSendService.sendLiveEventToUsers(socketSessionIds, liveEventMessage);
  }

  private void validateEventType(LiveEventMessage liveEventMessage) {
    try {
      EventType.fromValue(liveEventMessage.getEventType().toString());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

}
