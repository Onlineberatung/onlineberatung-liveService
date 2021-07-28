package de.caritas.cob.liveservice.websocket.stomphandler;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import de.caritas.cob.liveservice.websocket.service.KeycloakTokenObserver;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompCommand;

@ExtendWith(MockitoExtension.class)
class StompHandlerRegistryTest {

  @InjectMocks
  StompHandlerRegistry stompHandlerRegistry;

  @Mock
  ApplicationContext applicationContext;

  @ParameterizedTest
  @MethodSource("stompCommandWithoutHandler")
  void retrieveStompHandler_Should_returnNull_When_noHandlerForStompCommandIsImplemented(
      StompCommand stompCommand) {
    var stompHandler = this.stompHandlerRegistry.retrieveStompHandler(stompCommand);

    assertNull(stompHandler);
  }

  static List<StompCommand> stompCommandWithoutHandler() {
    return asList(null, StompCommand.STOMP, StompCommand.MESSAGE, StompCommand.ABORT,
        StompCommand.NACK, StompCommand.BEGIN, StompCommand.COMMIT, StompCommand.UNSUBSCRIBE,
        StompCommand.RECEIPT);
  }

  @ParameterizedTest
  @MethodSource("stompCommandWithHandler")
  void retrieveStompHandler_Should_returnExpectedHandler_When_handlerForCommandExists(
      StompHandler validHandler) {
    when(this.applicationContext.getBeansOfType(StompHandler.class))
        .thenReturn(Map.of(validHandler.supportedStompCommand().name(), validHandler));
    this.stompHandlerRegistry.initializeStompHandlers();

    var stompHandler = this.stompHandlerRegistry
        .retrieveStompHandler(validHandler.supportedStompCommand());

    assertThat(stompHandler, is(validHandler));
  }

  static List<StompHandler> stompCommandWithHandler() {
    return asList(
        new StompConnectHandler(mock(KeycloakTokenObserver.class), mock(SocketUserRegistry.class)),
        new StompSubscribeHandler(mock(SocketUserRegistry.class)),
        new StompAcknowledgeHandler(mock(LiveEventMessageQueue.class)),
        new StompDisconnectHandler(mock(SocketUserRegistry.class)),
        new StompErrorHandler(mock(SocketUserRegistry.class))
    );
  }

}
