package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * Class to handle the {@link StompCommand} error.
 */
@Component
public class StompErrorHandler extends StompRemoveHandler {

  @Autowired
  public StompErrorHandler(SocketUserRegistry socketUserRegistry) {
    super(requireNonNull(socketUserRegistry));
  }

  /**
   * The supported {@link StompCommand} of this class.
   *
   * @return the error {@link StompCommand}
   */
  @Override
  public StompCommand supportedStompCommand() {
    return StompCommand.ERROR;
  }

}
