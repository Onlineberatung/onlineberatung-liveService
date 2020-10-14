package de.caritas.cob.liveservice.service;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.model.WebSocketUserSession;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class SocketUserRegistry {

  private static final List<WebSocketUserSession> SUBSCRIBED_USERS = new CopyOnWriteArrayList<>();

  public synchronized void addUser(WebSocketUserSession webSocketUserSession) {
    SUBSCRIBED_USERS.add(webSocketUserSession);
  }

  public synchronized void removeSession(String sessionId) {
    WebSocketUserSession sessionToRemove = findUserBySessionId(sessionId);
    if (nonNull(sessionToRemove)) {
      SUBSCRIBED_USERS.remove(sessionToRemove);
    }
  }

  public synchronized WebSocketUserSession findUserBySessionId(String socketSessionId) {
    return SUBSCRIBED_USERS.stream()
        .filter(userSession -> socketSessionId.equals(userSession.getWebsocketSessionId()))
        .findFirst()
        .orElse(null);
  }

  public synchronized List<WebSocketUserSession> retrieveAllUsers() {
    return new LinkedList<>(SUBSCRIBED_USERS);
  }

}
