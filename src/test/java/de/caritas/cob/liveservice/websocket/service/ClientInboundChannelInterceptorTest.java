package de.caritas.cob.liveservice.websocket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import de.caritas.cob.liveservice.websocket.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
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
public class ClientInboundChannelInterceptorTest {

  @InjectMocks
  private ClientInboundChannelInterceptor clientInboundChannelInterceptor;

  @Mock
  private SocketUserRegistry socketUserRegistry;

  @Mock
  private KeycloakTokenObserver keycloakTokenObserver;

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
  public void preSend_Should_returnUntouchedMessage_When_accessorIsNull() {
    when(MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
        .thenReturn(null);
    Message<?> resultMessage = clientInboundChannelInterceptor.preSend(message, null);

    assertThat(resultMessage, is(message));
  }

  @Test
  public void preSend_Should_verifyKeycloakToken_When_accessorCommandIsConnect()
      throws VerificationException {
    when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.CONNECT);

    clientInboundChannelInterceptor.preSend(message, null);

    verify(keycloakTokenObserver, times(1)).observeUserId(any());
    verify(socketUserRegistry, times(1)).addUser(any());
  }

  @Test(expected = InvalidAccessTokenException.class)
  public void preSend_Should_throwInvalidAccessTokenException_When_tokenIsInvalid()
      throws VerificationException {
    when(keycloakTokenObserver.observeUserId(any())).thenThrow(new VerificationException(""));
    when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.CONNECT);

    clientInboundChannelInterceptor.preSend(message, null);
  }

  @Test
  public void preSend_Should_subscribeUser_When_accessorCommandIsSubscribe() {
    when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
    WebSocketUserSession socketUserSession = mock(WebSocketUserSession.class);
    when(socketUserRegistry.findUserBySessionId(any())).thenReturn(socketUserSession);

    clientInboundChannelInterceptor.preSend(message, null);

    verify(socketUserRegistry, times(1)).findUserBySessionId(any());
    verify(socketUserSession, times(1)).setSubscriptionId(any());
  }

  @Test
  public void preSend_Should_removeUser_When_accessorCommandIsDisconnect() {
    when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.DISCONNECT);

    clientInboundChannelInterceptor.preSend(message, null);

    verify(socketUserRegistry, times(1)).removeSession(any());
  }

  @Test
  public void preSend_Should_removeUser_When_accessorCommandIsError() {
    when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.ERROR);

    clientInboundChannelInterceptor.preSend(message, null);

    verify(socketUserRegistry, times(1)).removeSession(any());
  }

}
