package de.caritas.cob.liveservice.api.facade;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.api.model.VideoCallRequestDTO;
import de.caritas.cob.liveservice.websocket.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.liveeventmessage.BasicLiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.liveeventmessage.VideoCallLiveEventMessage;
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
   * @param userIds the target keycloak user ids
   * @param eventType the type of the event message
   */
  public void triggerLiveEvent(List<String> userIds, String eventType) {
    EventType validatedType = obtainValidatedEventType(eventType);
    List<String> socketSessionIds = resolveUserIds(userIds);
    BasicLiveEventMessage basicLiveEventMessage = BasicLiveEventMessage.builder()
        .eventType(validatedType.toString())
        .build();
    this.liveEventSendService.sendLiveEventToUsers(socketSessionIds, basicLiveEventMessage);
  }

  private EventType obtainValidatedEventType(String eventType) {
    try {
      return EventType.fromValue(eventType);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Triggers a video call request to give registered users.
   *
   * @param userIds the target keycloak user ids
   * @param videoCallRequestDto the video call information as {@link VideoCallRequestDTO} instance
   */
  public void triggerVideoCallRequest(List<String> userIds, VideoCallRequestDTO videoCallRequestDto) {
    List<String> socketSessionIds = resolveUserIds(userIds);
    VideoCallLiveEventMessage videoCallLiveEventMessage = fromVideoCallRequestDto(
        videoCallRequestDto);
    this.liveEventSendService.sendLiveEventToUsers(socketSessionIds, videoCallLiveEventMessage);
  }

  private VideoCallLiveEventMessage fromVideoCallRequestDto(
      VideoCallRequestDTO videoCallRequestDto) {
    return VideoCallLiveEventMessage.builder()
        .eventType(videoCallRequestDto.getEventType().toString())
        .rcRoomId(videoCallRequestDto.getRcRoomId())
        .videoChatUrl(videoCallRequestDto.getVideoChatUrl())
        .usernameConsultant(videoCallRequestDto.getUsernameConsultant())
        .build();
  }

  private List<String> resolveUserIds(List<String> userIds) {
    return this.sessionIdResolver.resolveUserIds(userIds);
  }

}
