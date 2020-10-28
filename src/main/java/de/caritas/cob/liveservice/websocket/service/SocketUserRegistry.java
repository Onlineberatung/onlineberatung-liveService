package de.caritas.cob.liveservice.websocket.service;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * Registry to hold and handle all current registered users.
 */
@Component
public class SocketUserRegistry {

  private static final List<WebSocketUserSession> SUBSCRIBED_USERS = new CopyOnWriteArrayList<>();

  /**
   * Adds the given {@link WebSocketUserSession} to the registry.
   *
   * @param webSocketUserSession the user session to be added
   */
  public synchronized void addUser(WebSocketUserSession webSocketUserSession) {
    SUBSCRIBED_USERS.add(webSocketUserSession);
  }

  /**
   * Removes the user session if a session with given id exists.
   *
   * @param sessionId the session id to identify the user session to remove
   */
  public synchronized void removeSession(String sessionId) {
    WebSocketUserSession sessionToRemove = findUserBySessionId(sessionId);
    if (nonNull(sessionToRemove)) {
      SUBSCRIBED_USERS.remove(sessionToRemove);
    }
  }

  /**
   * Finds a user by given session id.
   *
   * @param socketSessionId the socket session id to search for
   * @return the {@link WebSocketUserSession} or null if session does not exist
   */
  public synchronized WebSocketUserSession findUserBySessionId(String socketSessionId) {
    return SUBSCRIBED_USERS.stream()
        .filter(userSession -> socketSessionId.equals(userSession.getWebsocketSessionId()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns all current registered socket session users.
   *
   * @return all socket session users
   */
  public synchronized List<WebSocketUserSession> retrieveAllUsers() {
    return new LinkedList<>(SUBSCRIBED_USERS);
  }

}
