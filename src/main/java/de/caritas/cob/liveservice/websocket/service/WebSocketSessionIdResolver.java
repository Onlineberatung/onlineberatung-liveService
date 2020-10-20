package de.caritas.cob.liveservice.websocket.service;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolver to find all registered session ids with given user ids
 */
@Component
@RequiredArgsConstructor
public class WebSocketSessionIdResolver {

  private final @NonNull SocketUserRegistry socketUserRegistry;

  /**
   * Resolves user ids to websocket session ids.
   *
   * @param userIds the user ids to search for
   * @return all current registered socket session ids
   */
  public List<String> resolveUserIds(List<String> userIds) {
    if (isEmpty(userIds)) {
      return emptyList();
    }
    return socketUserRegistry.retrieveAllUsers().stream()
        .filter(socketUser -> userIds.contains(socketUser.getUserId()))
        .map(WebSocketUserSession::getWebsocketSessionId)
        .collect(Collectors.toList());
  }

}
