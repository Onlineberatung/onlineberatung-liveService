package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Objects.requireNonNull;
import static org.springframework.messaging.support.MessageHeaderAccessor.getAccessor;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * Representation of one stomp handler.
 */
public interface StompHandler {

  void handle(Message<?> inboundMessage);

  StompCommand supportedStompCommand();

  /**
   * Extracts the header field with the given name of the given {@link Message}.
   *
   * @param message the {@link Message}
   * @param headerName the header name to be extracted
   * @return the value of the message header
   */
  default String extractHeaderField(Message<?> message, String headerName) {
    var headers = message.getHeaders();
    return requireNonNull(headers.get(headerName)).toString();
  }

  /**
   * Extracts the session id of the given {@link Message}.
   *
   * @param message the {@link Message}
   * @return the value of the message id
   */
  default String extractSessionId(Message<?> message) {
    return extractHeaderField(message, "simpSessionId");
  }

  /**
   * Extracts the first native header field with the given name of the given {@link Message}.
   *
   * @param message the {@link Message}
   * @param headerName the native header name to be extracted
   * @return the value of the message header
   */
  default String extractFirstNativeHeader(Message<?> message, String headerName) {
    var accessor = requireNonNull(getAccessor(message, StompHeaderAccessor.class));
    return accessor.getFirstNativeHeader(headerName);
  }

}
