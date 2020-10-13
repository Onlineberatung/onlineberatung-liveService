package de.caritas.cob.liveservice.service;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.keycloak.adapters.rotation.AdapterRSATokenVerifier.verifyToken;
import static org.springframework.messaging.support.MessageHeaderAccessor.getAccessor;

import de.caritas.cob.liveservice.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.model.WebSocketUserSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientInboundChannelInterceptor implements ChannelInterceptor {

  private static final String SUBSCRIPTION_ID = "simpSubscriptionId";
  private static final String SESSION_ID = "simpSessionId";
  private static final String KEYCLOAK_USER_ID = "userId";
  private static final String ACCESS_TOKEN = "accessToken";
  
  private final @NonNull KeycloakSpringBootProperties keyCloakConfiguration;
  private final @NonNull SubscribedSocketUserService subscribedSocketUserService;

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
      this.subscribedSocketUserService.removeSession(socketSessionId);
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
      AccessToken accessToken = verifyToken(token,
          KeycloakDeploymentBuilder.build(this.keyCloakConfiguration));
      WebSocketUserSession webSocketUserSession = WebSocketUserSession.builder()
          .userId(accessToken.getOtherClaims().get(KEYCLOAK_USER_ID).toString())
          .websocketSessionId(socketSessionId)
          .build();

      this.subscribedSocketUserService.addUser(webSocketUserSession);
    } catch (VerificationException e) {
      throw new InvalidAccessTokenException(e);
    }
  }

  private void subscribe(Message<?> message, String socketSessionId) {
    String subscriptionId = observerHeaderField(message, SUBSCRIPTION_ID);
    this.subscribedSocketUserService.findUserBySessionId(socketSessionId)
        .setSubscriptionId(subscriptionId);
  }

}
