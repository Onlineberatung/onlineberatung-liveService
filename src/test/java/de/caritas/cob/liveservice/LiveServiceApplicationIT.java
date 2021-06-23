package de.caritas.cob.liveservice;

import static de.caritas.cob.liveservice.api.controller.LiveControllerIT.LIVEEVENT_SEND;
import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static de.caritas.cob.liveservice.api.model.EventType.VIDEOCALLDENY;
import static de.caritas.cob.liveservice.api.model.EventType.VIDEOCALLREQUEST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.api.model.VideoCallRequestDTO;
import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing")
class LiveServiceApplicationIT extends StompClientIntegrationTest {

  @Autowired
  private SocketUserRegistry socketUserRegistry;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void connectToSocket_Should_connect_When_accessTokenIsValid() throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);

    assertThat(stompSession.isConnected(), is(true));
    performDisconnect(stompSession);
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
    final List<StompSession> stompSessions = new ArrayList<>();

    for (int i = 0; i < 500; i++) {
      stompSessions.add(performConnect(FIRST_VALID_USER));
    }

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(500));
    stompSessions.forEach(this::performDisconnect);
  }

  @Test
  void connectToSocket_Should_registerExpectedUser_When_accessTokenIsValid() throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);

    var registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);

    performDisconnect(stompSession);
    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user 1"));
    assertThat(registeredUser.getSubscriptionId(), nullValue());
  }

  @Test
  void subscribe_Should_subscribeUser() throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    final Subscription subscription = performSubscribe(stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    WebSocketUserSession registeredUser = this.socketUserRegistry.retrieveAllUsers().get(0);
    await()
        .atMost(MESSAGE_TIMEOUT, SECONDS)
        .until(registeredUser::getSubscriptionId, notNullValue());

    assertThat(registeredUser, notNullValue());
    assertThat(registeredUser.getWebsocketSessionId(), notNullValue());
    assertThat(registeredUser.getUserId(), is("validated user 1"));
    assertThat(registeredUser.getSubscriptionId(), notNullValue());
    assertThat(subscription.getSubscriptionHeaders().get("destination"),
        contains(SUBSCRIPTION_ENDPOINT));
    performDisconnect(stompSession);
  }

  @Test
  void disconnect_Should_removeUserFromRegistry() throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(1));
    performDisconnect(stompSession);

    assertThat(this.socketUserRegistry.retrieveAllUsers(), hasSize(0));
  }

  @Test
  void sendLiveEvent_Should_sendDirectMessageEventToUser_When_userIsSubscribed() throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(DIRECTMESSAGE, singletonList("validated user 1"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    var resultMessage = receivedMessages.poll(MESSAGE_TIMEOUT, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is(DIRECTMESSAGE));
    performDisconnect(stompSession);
    receivedMessages.clear();
  }

  @Test
  void sendLiveEvent_Should_sendVideoCallRequestMessageEventToUser_When_userIsSubscribed()
      throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    var eventContent = new EasyRandom().nextObject(VideoCallRequestDTO.class);

    performSubscribe(stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(VIDEOCALLREQUEST, singletonList("validated user 1"),
            eventContent))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    var resultMessage = receivedMessages.poll(MESSAGE_TIMEOUT, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is(VIDEOCALLREQUEST));
    var resultContent = new ObjectMapper()
        .readValue(new ObjectMapper().writeValueAsString(resultMessage.getEventContent()),
            VideoCallRequestDTO.class);
    assertThat(resultContent, is(eventContent));
    performDisconnect(stompSession);
    receivedMessages.clear();
  }

  @Test
  void sendLiveEvent_Should_sendVideoDenyRequestMessageEventToUser_When_userIsSubscribed()
      throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(VIDEOCALLDENY, singletonList("validated user 1"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    var resultMessage = receivedMessages.poll(MESSAGE_TIMEOUT, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is(VIDEOCALLDENY));
    performDisconnect(stompSession);
    receivedMessages.clear();
  }

  @Test
  void sendLiveEvents_Should_sendEventsToExpectedUsers_When_usersAreSubscribed() throws Exception {
    var firstStompSession = performConnect(FIRST_VALID_USER);
    var secondStompSession = performConnect(SECOND_VALID_USER);
    var thirdStompSession = performConnect(THIRD_VALID_USER);

    BlockingQueue<LiveEventMessage> firstUserMessages = new ArrayBlockingQueue<>(1);
    BlockingQueue<LiveEventMessage> secondUserMessages = new ArrayBlockingQueue<>(2);
    BlockingQueue<LiveEventMessage> thirdUserMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(firstStompSession, firstUserMessages);
    performSubscribe(secondStompSession, secondUserMessages);
    performSubscribe(thirdStompSession, thirdUserMessages);

    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(DIRECTMESSAGE,
            asList("validated user 1", "validated user 2", "validated user 3"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(DIRECTMESSAGE, singletonList("validated user 2"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    assertThat(firstUserMessages.poll(MESSAGE_TIMEOUT, SECONDS), notNullValue());
    assertThat(secondUserMessages.poll(MESSAGE_TIMEOUT, SECONDS), notNullValue());
    assertThat(secondUserMessages.poll(MESSAGE_TIMEOUT, SECONDS), notNullValue());
    assertThat(thirdUserMessages.poll(MESSAGE_TIMEOUT, SECONDS), notNullValue());
    assertThat(firstUserMessages, hasSize(0));
    assertThat(secondUserMessages, hasSize(0));
    assertThat(thirdUserMessages, hasSize(0));
    performDisconnect(firstStompSession);
    performDisconnect(secondStompSession);
    performDisconnect(thirdStompSession);
    firstUserMessages.clear();
    secondUserMessages.clear();
    thirdUserMessages.clear();
  }

}
