package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;

/**
 * Class to handle removing of registered users.
 */
@RequiredArgsConstructor
abstract class StompRemoveHandler implements StompHandler {

  private final @NonNull SocketUserRegistry socketUserRegistry;

  /**
   * Removes the socket user with the socket session id of the given inbound {@link Message}.
   *
   * @param inboundMessage the message containing the socket session id to be removed
   */
  @Override
  public void handle(Message<?> inboundMessage) {
    if (nonNull(inboundMessage)) {
      var socketSessionId = extractSessionId(inboundMessage);
      this.socketUserRegistry.removeSession(socketSessionId);
    }
  }

}
