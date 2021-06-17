package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.nonNull;

import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * Class to handle the {@link StompCommand} ack.
 */
@Component
@RequiredArgsConstructor
public class StompAcknowledgeHandler implements StompHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(StompAcknowledgeHandler.class);
  private static final String MESSAGE_ID = "message-id";

  private final @NonNull LiveEventMessageQueue liveEventMessageQueue;

  /**
   * Handles the acknowledge {@link StompCommand}.
   *
   * @param inboundMessage the incoming websocket message
   */
  @Override
  public void handle(Message<?> inboundMessage) {
    if (nonNull(inboundMessage)) {
      LOGGER.info("Inbound Message = {}", inboundMessage);
      var messageId = extractFirstNativeHeader(inboundMessage, MESSAGE_ID);
      this.liveEventMessageQueue.removeIdentifiedMessageWithId(messageId);
    }
  }

  /**
   * The supported {@link StompCommand} of this class.
   *
   * @return the acknowledge {@link StompCommand}
   */
  @Override
  public StompCommand supportedStompCommand() {
    return StompCommand.ACK;
  }

}
