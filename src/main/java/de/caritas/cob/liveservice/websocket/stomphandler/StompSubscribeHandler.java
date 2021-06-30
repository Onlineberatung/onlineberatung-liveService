package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * Class to handle the {@link StompCommand} subscribe.
 */
@Component
@RequiredArgsConstructor
public class StompSubscribeHandler implements StompHandler {

  private static final String SUBSCRIPTION_ID = "simpSubscriptionId";

  private final @NonNull SocketUserRegistry socketUserRegistry;

  /**
   * Handles the subscribe {@link StompCommand}.
   *
   * @param inboundMessage the incoming websocket message
   */
  @Override
  public void handle(Message<?> inboundMessage) {
    if (nonNull(inboundMessage)) {
      var socketSessionId = extractSessionId(inboundMessage);

      subscribe(inboundMessage, socketSessionId);
    }
  }

  private void subscribe(Message<?> message, String socketSessionId) {
    String subscriptionId = extractHeaderField(message, SUBSCRIPTION_ID);
    this.socketUserRegistry.findUserBySessionId(socketSessionId).setSubscriptionId(subscriptionId);
  }

  /**
   * The supported {@link StompCommand} of this class.
   *
   * @return the subscribe {@link StompCommand}
   */
  @Override
  public StompCommand supportedStompCommand() {
    return StompCommand.SUBSCRIBE;
  }
}
