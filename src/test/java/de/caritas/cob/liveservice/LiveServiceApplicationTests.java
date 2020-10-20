package de.caritas.cob.liveservice;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.service.SocketUserRegistry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;

class LiveServiceApplicationTests extends StompClientIntegrationTest {

  @Autowired
  private SocketUserRegistry socketUserRegistry;

  @Test
  void connectToSocket_Should_connect_When_accessTokenIsValid() throws Exception {
    StompSession stompSession = performConnect("valid token");

    assertThat(stompSession.isConnected(), is(true));
  }

  @Test
  void connectToSocket_Should_throwExecutionException_When_accessTokenIsInValid() {
    try {
      performConnect(null);
    } catch (Exception e) {
      assertThat(e, instanceOf(ExecutionException.class));
    }
  }

  @Test
  void connectMultipleUsersToSocket_Should_register_When_accessTokensAreValid() throws Exception {
    for (int i = 0; i < 500; i++) {
      performConnect("valid token");
    }

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(500));
  }

  @Test
  void connectToSocket_Should_registerExpectedUser_When_accessTokenIsValid() throws Exception {
    performConnect("valid token");

    WebSocketUserSession registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);

    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user"));
    assertThat(registeredUser.getSubscriptionId(), nullValue());
  }

  @Test
  void subscribe_Should_subscribeUser() throws Exception {
    StompSession stompSession = performConnect("valid token");
    final Subscription subscription = performSubscribe("/events", stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    WebSocketUserSession registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);
    await()
        .atMost(2, TimeUnit.SECONDS)
        .until(registeredUser::getSubscriptionId, notNullValue());

    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user"));
    assertThat(registeredUser.getSubscriptionId(), notNullValue());
    assertThat(subscription.getSubscriptionHeaders().get("destination"), contains("/events"));
  }

  @Test
  void disconnect_Should_removeUserFromRegistry() throws Exception {
    StompSession stompSession = performConnect("valid token");

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    performDisconnect(stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(0));
  }

}
