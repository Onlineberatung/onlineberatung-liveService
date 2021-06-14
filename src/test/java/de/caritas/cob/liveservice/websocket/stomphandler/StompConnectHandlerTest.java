package de.caritas.cob.liveservice.websocket.stomphandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import de.caritas.cob.liveservice.websocket.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import de.caritas.cob.liveservice.websocket.service.KeycloakTokenObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.common.VerificationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageHeaderAccessor.class)
public class StompConnectHandlerTest {

  @InjectMocks
  private StompConnectHandler stompConnectHandler;

  @Mock
  private KeycloakTokenObserver keycloakTokenObserver;

  @Mock
  private SocketUserRegistry socketUserRegistry;

  @Mock
  private MessageHeaders messageHeaders;

  @Mock
  private StompHeaderAccessor stompHeaderAccessor;

  @Mock
  private Message<?> message;

  @Before
  public void initMocks() {
    when(messageHeaders.get(anyString())).thenReturn("header");
    when(message.getHeaders()).thenReturn(messageHeaders);
    mockStatic(MessageHeaderAccessor.class);
    when(MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
        .thenReturn(stompHeaderAccessor);
  }

  @Test
  public void supportedStompCommand_Should_returnConnect() {
    var command = this.stompConnectHandler.supportedStompCommand();

    assertThat(command, is(StompCommand.CONNECT));
  }

  @Test
  public void handle_Should_useNoServices_When_messageIsNull() {
    this.stompConnectHandler.handle(null);

    verifyNoInteractions(this.keycloakTokenObserver, this.socketUserRegistry);
  }

  @Test(expected = InvalidAccessTokenException.class)
  public void handle_Should_throwInvalidAccessTokenException_When_tokenIsInvalid()
      throws VerificationException {
    when(this.stompHeaderAccessor.getFirstNativeHeader(anyString())).thenReturn("accessToken");
    when(this.keycloakTokenObserver.observeUserId(anyString()))
        .thenThrow(new VerificationException());

    this.stompConnectHandler.handle(this.message);
  }

  @Test
  public void handle_Should_useAllServices_When_tokenIsValid() throws VerificationException {
    when(this.stompHeaderAccessor.getFirstNativeHeader(anyString())).thenReturn("accessToken");

    this.stompConnectHandler.handle(this.message);

    verify(this.keycloakTokenObserver, times(1)).observeUserId("accessToken");
    verify(this.socketUserRegistry, times(1)).addUser(any());
  }

}
