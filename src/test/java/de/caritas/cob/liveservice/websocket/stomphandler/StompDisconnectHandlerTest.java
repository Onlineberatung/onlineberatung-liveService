package de.caritas.cob.liveservice.websocket.stomphandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;

@ExtendWith(MockitoExtension.class)
class StompDisconnectHandlerTest {

  @InjectMocks
  StompDisconnectHandler stompDisconnectHandler;

  @Mock
  SocketUserRegistry socketUserRegistry;

  @Test
  void supportedStompCommand_Should_return_disconnect() {
    var stompCommand = this.stompDisconnectHandler.supportedStompCommand();

    assertThat(stompCommand, is(StompCommand.DISCONNECT));
  }

  @Test
  void handle_Should_useNoService_When_messageIsNull() {
    this.stompDisconnectHandler.handle(null);

    verifyNoMoreInteractions(this.socketUserRegistry);
  }

  @Test
  void handle_Should_removeExpectedSocketSession_When_messageIsValid() {
    var message = mock(Message.class);
    var messageHeaders = Map.of("simpSessionId", (Object) "123");
    when(message.getHeaders()).thenReturn(new MessageHeaders(messageHeaders));

    this.stompDisconnectHandler.handle(message);

    verify(this.socketUserRegistry, times(1)).removeSession("123");
  }

}
