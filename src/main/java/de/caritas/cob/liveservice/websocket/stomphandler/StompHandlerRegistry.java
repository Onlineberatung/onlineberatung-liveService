package de.caritas.cob.liveservice.websocket.stomphandler;

import java.util.EnumMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * Registry to hold all available {@link StompHandler} beans.
 */
@Component
@RequiredArgsConstructor
public class StompHandlerRegistry {

  private final @NonNull ApplicationContext applicationContext;
  private final Map<StompCommand, StompHandler> handlerForCommand = new EnumMap<>(
      StompCommand.class);

  /**
   * Initializes the registry with all available {@link StompHandler} beans.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initializeStompHandlers() {
    this.applicationContext.getBeansOfType(StompHandler.class).values()
        .forEach(this::addHandlerForCommand);
  }

  private void addHandlerForCommand(StompHandler stompHandler) {
    this.handlerForCommand.put(stompHandler.supportedStompCommand(), stompHandler);
  }

  /**
   * Retrieves the {@link StompHandler} by the given {@link StompCommand}.
   *
   * @param stompCommand the {@link StompCommand} to query for
   * @return the responsible {@link StompHandler}
   */
  public StompHandler retrieveStompHandler(StompCommand stompCommand) {
    return this.handlerForCommand.get(stompCommand);
  }

}
