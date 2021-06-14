package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;

@ExtendWith(MockitoExtension.class)
class StompSubscribeHandlerTest {

  @InjectMocks
  StompSubscribeHandler stompSubscribeHandler;

  @Mock
  SocketUserRegistry socketUserRegistry;

  @Test
  void supportedStompCommand_Should_return_subscribe() {
    var stompCommand = this.stompSubscribeHandler.supportedStompCommand();

    assertThat(stompCommand, is(StompCommand.SUBSCRIBE));
  }

  @Test
  void handle_Should_useNoServices_When_messageIsNull() {
    this.stompSubscribeHandler.handle(null);

    verifyNoMoreInteractions(this.socketUserRegistry);
  }

  @Test
  void handle_Should_subscribeUser_When_messageIsValid() {
    var socketUserSession = mock(WebSocketUserSession.class);
    when(this.socketUserRegistry.findUserBySessionId(anyString())).thenReturn(socketUserSession);
    var message = mock(Message.class);
    var messageHeaders = Map.of("simpSessionId", (Object) "123", "simpSubscriptionId", "345");
    when(message.getHeaders()).thenReturn(new MessageHeaders(messageHeaders));

    this.stompSubscribeHandler.handle(message);

    verify(this.socketUserRegistry, times(1)).findUserBySessionId("123");
    verify(socketUserSession, times(1)).setSubscriptionId("345");
  }

  @ParameterizedTest
  @MethodSource("messageWithMissingHeader")
  void handle_Should_throwNullPointerException_When_headerIsMissing(Map<String, Object> headers) {
    var message = mock(Message.class);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));

    assertThrows(NullPointerException.class, () -> this.stompSubscribeHandler.handle(message));
  }

  static List<Map<String, Object>> messageWithMissingHeader() {
    return List.of(
        emptyMap(),
        Map.of("simpSessionId", "123"),
        Map.of("simpSubscriptionId", "345"),
        Map.of("other", "123", "stomp", "345", "js", "678")
    );
  }

}
