package de.caritas.cob.liveservice;

import static java.util.Collections.singletonList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.service.KeycloakTokenObserver;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.runner.RunWith;
import org.keycloak.common.VerificationException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = StompClientIntegrationTest.TestConfig.class)
public abstract class StompClientIntegrationTest extends AbstractJUnit4SpringContextTests {

  protected static final String SUBSCRIPTION_ENDPOINT = "/user/events";
  protected static final int MESSAGE_TIMEOUT = 10;
  protected static final String FIRST_VALID_USER = "firstValidUser";
  static final String SECOND_VALID_USER = "secondValidUser";
  static final String THIRD_VALID_USER = "thirdValidUser";

  private static final String SOCKET_URL = "ws://localhost:%d/live";
  private final StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
  };

  @LocalServerPort
  private Integer port;

  private final WebSocketStompClient socketStompClient = new WebSocketStompClient(
      new SockJsClient(singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

  @Configuration
  @Import(LiveServiceApplication.class)
  public static class TestConfig {

    @Bean
    public KeycloakTokenObserver keycloakTokenObserver() throws VerificationException {
      KeycloakTokenObserver observer = mock(KeycloakTokenObserver.class);
      when(observer.observeUserId(FIRST_VALID_USER)).thenReturn("validated user 1");
      when(observer.observeUserId(SECOND_VALID_USER)).thenReturn("validated user 2");
      when(observer.observeUserId(THIRD_VALID_USER)).thenReturn("validated user 3");
      when(observer.observeUserId(null)).thenThrow(new VerificationException("invalid"));
      return observer;
    }
  }

  protected StompSession performConnect(String accessToken)
      throws ExecutionException, InterruptedException, TimeoutException {
    socketStompClient.setMessageConverter(new MessageConverter() {
      @SneakyThrows
      @Override
      public Object fromMessage(Message<?> message, Class<?> targetType) {
        return new ObjectMapper().readValue((byte[]) message.getPayload(), targetType);
      }

      @Override
      public Message<?> toMessage(Object o, MessageHeaders messageHeaders) {
        return null;
      }
    });
    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.add("accessToken", accessToken);
    ListenableFuture<StompSession> connect = socketStompClient.connect(
        String.format(SOCKET_URL, port), new WebSocketHttpHeaders(), connectHeaders,
        sessionHandler);
    return connect.get(3, TimeUnit.SECONDS);
  }

  protected Subscription performSubscribe(StompSession stompSession) {
    return stompSession.subscribe(StompClientIntegrationTest.SUBSCRIPTION_ENDPOINT, sessionHandler);
  }

  protected synchronized void performSubscribe(StompSession stompSession,
      List<LiveEventMessage> eventCollector) {
    stompSession
        .subscribe(StompClientIntegrationTest.SUBSCRIPTION_ENDPOINT, new StompFrameHandler() {
          @Override
          public Type getPayloadType(StompHeaders headers) {
            return LiveEventMessage.class;
          }

          @Override
          public synchronized void handleFrame(StompHeaders headers, Object payload) {
            eventCollector.add((LiveEventMessage) payload);
          }
        });
  }

  protected void performDisconnect(StompSession stompSession) {
    stompSession.disconnect();
    await().until(stompSession::isConnected, equalTo(false));
  }

  public static String buildLiveEventMessage(EventType eventType,
      List<String> userIds, Object eventContent) {
    try {
      return new ObjectMapper().writeValueAsString(
          new LiveEventMessage()
              .eventType(eventType)
              .userIds(userIds)
              .eventContent(eventContent));
    } catch (JsonProcessingException e) {
      throw new RuntimeJsonMappingException(e.getMessage());
    }
  }

}
