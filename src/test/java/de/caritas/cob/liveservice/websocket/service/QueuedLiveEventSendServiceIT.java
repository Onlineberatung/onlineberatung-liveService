package de.caritas.cob.liveservice.websocket.service;

import static de.caritas.cob.liveservice.api.controller.LiveControllerIT.LIVEEVENT_SEND;
import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.liveservice.StompClientIntegrationTest;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.websocket.registry.LiveEventMessageQueue;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=queuedtesting")
class QueuedLiveEventSendServiceIT extends StompClientIntegrationTest {

  @Autowired
  private LiveEventMessageQueue liveEventMessageQueue;

  @Autowired
  private SocketUserRegistry socketUserRegistry;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void sendLiveEvent_Should_sendDirectMessageMultipleTimesToUserAndFinalyRemove_When_clientDoesNotAcknowledge()
      throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(SUBSCRIPTION_ENDPOINT, stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(DIRECTMESSAGE, singletonList("validated user 1"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    var resultMessage = receivedMessages.poll(MESSAGE_TIMEOUT, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is(DIRECTMESSAGE));
    for (int i = 0; i < 5; i++) {
      var furtherMessage = receivedMessages.poll(MESSAGE_TIMEOUT * 10, SECONDS);
      assertThat(furtherMessage, notNullValue());
      assertThat(furtherMessage.getEventType(), is(DIRECTMESSAGE));
    }
    await()
        .atMost(MESSAGE_TIMEOUT, SECONDS)
        .until(this.liveEventMessageQueue::getCurrentOpenMessages, hasSize(0));
  }

  @Test
  void sendLiveEvent_Should_sendDirectMessageToUserViaQueue_When_clientReconnectsUser()
      throws Exception {
    var stompSession = performConnect(FIRST_VALID_USER);
    BlockingQueue<LiveEventMessage> receivedMessages = new ArrayBlockingQueue<>(1);

    performSubscribe(SUBSCRIPTION_ENDPOINT, stompSession, receivedMessages);
    mockMvc.perform(post(LIVEEVENT_SEND)
        .contentType(APPLICATION_JSON)
        .content(buildLiveEventMessage(DIRECTMESSAGE, singletonList("validated user 1"), null))
        .contentType(APPLICATION_JSON))
        .andExpect(status().isOk());

    receivedMessages.clear();
    performDisconnect(stompSession);
    var newStompSession = performConnect(FIRST_VALID_USER);
    performSubscribe(SUBSCRIPTION_ENDPOINT, newStompSession, receivedMessages);

    var resultMessage = receivedMessages.poll(MESSAGE_TIMEOUT, SECONDS);
    assertThat(resultMessage, notNullValue());
    assertThat(resultMessage.getEventType(), is(DIRECTMESSAGE));
  }

}
