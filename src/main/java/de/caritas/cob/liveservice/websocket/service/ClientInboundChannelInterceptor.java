package de.caritas.cob.liveservice.websocket.service;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.springframework.messaging.support.MessageHeaderAccessor.getAccessor;

import de.caritas.cob.liveservice.websocket.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.common.VerificationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

/**
 * Interceptor to handle all client inbound messages.
 */
@Service
@RequiredArgsConstructor
public class ClientInboundChannelInterceptor implements ChannelInterceptor {

  private static final String SUBSCRIPTION_ID = "simpSubscriptionId";
  private static final String SESSION_ID = "simpSessionId";
  private static final String ACCESS_TOKEN = "accessToken";
  
  private final @NonNull SocketUserRegistry socketUserRegistry;
  private final @NonNull KeycloakTokenObserver keyCloakTokenObserver;

  /**
   * Method invocation everytime a socket message is send to the server and must be handled.
   *
   * @param message the {@link Message} to be handled
   * @param channel the {@link MessageChannel}
   * @return the intercepted message
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = getAccessor(message, StompHeaderAccessor.class);
    String socketSessionId = observerHeaderField(message, SESSION_ID);

    if (isNull(accessor)) {
      return message;
    }

    handleStompCommand(message, accessor, socketSessionId);

    return message;
  }

  private void handleStompCommand(Message<?> message, StompHeaderAccessor accessor,
      String socketSessionId) {
    
    StompCommand accessorCommand = accessor.getCommand();
    if (StompCommand.CONNECT.equals(accessorCommand)) {
      verifyTokenAndAddSocketSessionUser(accessor, socketSessionId);
    } else if (StompCommand.SUBSCRIBE.equals(accessorCommand)) {
      subscribe(message, socketSessionId);
    } else if (StompCommand.DISCONNECT.equals(accessorCommand) || StompCommand.ERROR.equals(
        accessorCommand)) {
      this.socketUserRegistry.removeSession(socketSessionId);
    }
  }

  private String observerHeaderField(Message<?> message, String headerName) {
    MessageHeaders headers = message.getHeaders();
    return requireNonNull(headers.get(headerName)).toString();
  }

  private void verifyTokenAndAddSocketSessionUser(StompHeaderAccessor accessor,
      String socketSessionId) {
    String token = accessor.getFirstNativeHeader(ACCESS_TOKEN);
    try {
      String userId = this.keyCloakTokenObserver.observeUserId(token);
      WebSocketUserSession webSocketUserSession = WebSocketUserSession.builder()
          .userId(userId)
          .websocketSessionId(socketSessionId)
          .build();

      this.socketUserRegistry.addUser(webSocketUserSession);
    } catch (VerificationException e) {
      throw new InvalidAccessTokenException("Token is invalid");
    }
  }

  private void subscribe(Message<?> message, String socketSessionId) {
    String subscriptionId = observerHeaderField(message, SUBSCRIPTION_ID);
    this.socketUserRegistry.findUserBySessionId(socketSessionId)
        .setSubscriptionId(subscriptionId);
  }

}
