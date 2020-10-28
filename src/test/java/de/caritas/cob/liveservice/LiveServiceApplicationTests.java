package de.caritas.cob.liveservice;

import static de.caritas.cob.liveservice.api.controller.LiveControllerIT.LIVEEVENT_SEND;
import static de.caritas.cob.liveservice.api.controller.LiveControllerIT.USER_IDS_PARAM;
import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.liveservice.websocket.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.service.SocketUserRegistry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class LiveServiceApplicationTests extends StompClientIntegrationTest {

  private static final String SUBSCRIPTION_ENDPOINT = "/user/events";

  @Autowired
  private SocketUserRegistry socketUserRegistry;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void connectToSocket_Should_connect_When_accessTokenIsValid() throws Exception {
    StompSession stompSession = performConnect(FIRST_VALID_USER);

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
      performConnect(FIRST_VALID_USER);
    }

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(500));
  }

  @Test
  void connectToSocket_Should_registerExpectedUser_When_accessTokenIsValid() throws Exception {
    performConnect(FIRST_VALID_USER);

    WebSocketUserSession registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);

    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user 1"));
    assertThat(registeredUser.getSubscriptionId(), nullValue());
  }

  @Test
  void subscribe_Should_subscribeUser() throws Exception {
    StompSession stompSession = performConnect(FIRST_VALID_USER);
    final Subscription subscription = performSubscribe(SUBSCRIPTION_ENDPOINT, stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    WebSocketUserSession registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);
    await()
        .atMost(2, SECONDS)
        .until(registeredUser::getSubscriptionId, notNullValue());

    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user 1"));
    assertThat(registeredUser.getSubscriptionId(), notNullValue());
    assertThat(subscription.getSubscriptionHeaders().get("destination"),
        contains(SUBSCRIPTION_ENDPOINT));
  }

  @Test
  void disconnect_Should_removeUserFromRegistry() throws Exception {
    StompSession stompSession = performConnect(FIRST_VALID_USER);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    performDisconnect(stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(0));
  }

  @Test
  void sendLiveEvent_Should_sendEventToUser_When_userIsSubscribed() throws Exception {
    StompSession stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(SUBSCRIPTION_ENDPOINT, stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(USER_IDS_PARAM, "validated user 1").contentType(APPLICATION_JSON)
        .content(DIRECTMESSAGE.toString()).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    LiveEventMessage resultMessage = receivedMessages.poll(1, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is("directMessage"));
  }

  @Test
  void sendLiveEvents_Should_sendEventsToExpectedUsers_When_usersAreSubscribed() throws Exception {
    StompSession firstStompSession = performConnect(FIRST_VALID_USER);
    StompSession secondStompSession = performConnect(SECOND_VALID_USER);
    StompSession thirdStompSession = performConnect(THIRD_VALID_USER);

    BlockingQueue<LiveEventMessage> firstUserMessages = new ArrayBlockingQueue<>(1);
    BlockingQueue<LiveEventMessage> secondUserMessages = new ArrayBlockingQueue<>(2);
    BlockingQueue<LiveEventMessage> thirdUserMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(SUBSCRIPTION_ENDPOINT, firstStompSession, firstUserMessages);
    performSubscribe(SUBSCRIPTION_ENDPOINT, secondStompSession, secondUserMessages);
    performSubscribe(SUBSCRIPTION_ENDPOINT, thirdStompSession, thirdUserMessages);

    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(USER_IDS_PARAM, "validated user 1", "validated user 2", "validated user 3")
        .contentType(APPLICATION_JSON)
        .content(DIRECTMESSAGE.toString()).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(USER_IDS_PARAM, "validated user 2")
        .contentType(APPLICATION_JSON)
        .content(DIRECTMESSAGE.toString()).contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    assertThat(firstUserMessages.poll(1, SECONDS), notNullValue());
    assertThat(secondUserMessages.poll(1, SECONDS), notNullValue());
    assertThat(secondUserMessages.poll(1, SECONDS), notNullValue());
    assertThat(thirdUserMessages.poll(1, SECONDS), notNullValue());
    assertThat(firstUserMessages, hasSize(0));
    assertThat(secondUserMessages, hasSize(0));
    assertThat(thirdUserMessages, hasSize(0));
  }

}
