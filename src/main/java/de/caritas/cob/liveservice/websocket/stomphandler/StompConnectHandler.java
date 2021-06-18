package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.websocket.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import de.caritas.cob.liveservice.websocket.service.KeycloakTokenObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.common.VerificationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * Class to handle the {@link StompCommand} connect.
 */
@Component
@RequiredArgsConstructor
public class StompConnectHandler implements StompHandler {

  private static final String ACCESS_TOKEN = "accessToken";

  private final @NonNull KeycloakTokenObserver keycloakTokenObserver;
  private final @NonNull SocketUserRegistry socketUserRegistry;

  /**
   * Handles the connect {@link StompCommand}.
   *
   * @param inboundMessage the incoming websocket message
   */
  @Override
  public void handle(Message<?> inboundMessage) {
    if (nonNull(inboundMessage)) {
      var socketSessionId = extractSessionId(inboundMessage);
      var accessToken = extractFirstNativeHeader(inboundMessage, ACCESS_TOKEN);

      verifyTokenAndAddSocketSessionUser(accessToken, socketSessionId);
    }
  }

  private void verifyTokenAndAddSocketSessionUser(String token, String socketSessionId) {
    try {
      var userId = this.keycloakTokenObserver.observeUserId(token);
      var webSocketUserSession = WebSocketUserSession.builder()
          .userId(userId)
          .websocketSessionId(socketSessionId)
          .build();

      this.socketUserRegistry.addUser(webSocketUserSession);
    } catch (VerificationException e) {
      throw new InvalidAccessTokenException("Token is invalid");
    }
  }

  /**
   * The supported {@link StompCommand} of this class.
   *
   * @return the connect {@link StompCommand}
   */
  @Override
  public StompCommand supportedStompCommand() {
    return StompCommand.CONNECT;
  }
}
