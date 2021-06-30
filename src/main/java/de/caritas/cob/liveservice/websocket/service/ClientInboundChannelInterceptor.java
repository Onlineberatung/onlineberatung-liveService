package de.caritas.cob.liveservice.websocket.service;

import static java.util.Objects.nonNull;
import static org.springframework.messaging.support.MessageHeaderAccessor.getAccessor;

import de.caritas.cob.liveservice.websocket.stomphandler.StompHandlerRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

/**
 * Interceptor to handle all client inbound messages.
 */
@Service
@RequiredArgsConstructor
public class ClientInboundChannelInterceptor implements ChannelInterceptor {

  private final @NonNull StompHandlerRegistry stompHandlerRegistry;

  /**
   * Method invocation everytime a socket message is send to the server and must be handled.
   *
   * @param message the {@link Message} to be handled
   * @param channel the {@link MessageChannel}
   * @return the intercepted message
   */
  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    var accessor = getAccessor(message, StompHeaderAccessor.class);
    if (nonNull(accessor)) {
      handleStompCommand(message, accessor);
    }

    return message;
  }

  private void handleStompCommand(Message<?> message, StompHeaderAccessor stompHeaderAccessor) {
    var stompCommand = stompHeaderAccessor.getCommand();
    var stompHandler = this.stompHandlerRegistry.retrieveStompHandler(stompCommand);
    if (nonNull(stompHandler)) {
      stompHandler.handle(message);
    }
  }

}
