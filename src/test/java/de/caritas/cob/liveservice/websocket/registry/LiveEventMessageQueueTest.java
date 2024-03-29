package de.caritas.cob.liveservice.websocket.registry;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LiveEventMessageQueueTest {

  private LiveEventMessageQueue liveEventMessageQueue;

  @BeforeEach
  void setup() {
    this.liveEventMessageQueue = new LiveEventMessageQueue();
    setInternalState(liveEventMessageQueue, "minimumSecondsBeforeRetry", 1);
  }

  @Test
  void addIdentifiedMessage_Should_addIdentifiedMessageToRegistry() {
    liveEventMessageQueue
        .addIdentifiedMessage(IdentifiedMessage.builder().messageId("messageid").createdDate(
            LocalDateTime.now(ZoneOffset.UTC).minus(3, SECONDS)).build());

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(1));
  }

  @Test
  void removeIdentifiedMessageWithId_Should_removeMessage_When_messageExists() {
    liveEventMessageQueue
        .addIdentifiedMessage(IdentifiedMessage.builder().messageId("messageid").createdDate(
            LocalDateTime.now(ZoneOffset.UTC).minus(3, SECONDS)).build());

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(1));
    liveEventMessageQueue.removeIdentifiedMessageWithId("messageid");

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(0));
  }

  @Test
  void removeIdentifiedMessageWithId_Should_notRemoveMessage_When_messageDoesNotExists() {
    liveEventMessageQueue
        .addIdentifiedMessage(IdentifiedMessage.builder().messageId("messageid").createdDate(
            LocalDateTime.now(ZoneOffset.UTC).minus(2, SECONDS)).build());

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(1));
    liveEventMessageQueue.removeIdentifiedMessageWithId("other");

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(1));
  }

  @Test
  void getCurrentOpenMessages_Should_alwaysReturnAllRegisteredQueuedMessages() {
    for (int incrementer = 0; incrementer < 5000; incrementer++) {
      liveEventMessageQueue.addIdentifiedMessage(
          IdentifiedMessage.builder()
              .messageId("message " + incrementer)
              .createdDate(LocalDateTime.now(ZoneOffset.UTC).minus(2, SECONDS))
              .build());
    }

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(5000));
    for (int incrementer = 0; incrementer < 2500; incrementer++) {
      liveEventMessageQueue.removeIdentifiedMessageWithId("message " + incrementer);
    }

    assertThat(liveEventMessageQueue.getCurrentOpenMessages(), hasSize(2500));
    var message = liveEventMessageQueue.getCurrentOpenMessages().iterator().next();
    assertThat(message.getMessageId(), is("message 2500"));
  }

  @Test
  void getCurrentOpenMessages_Should_removeNoMessage_When_minimumRetryLimitIsntReached() {
    liveEventMessageQueue
        .addIdentifiedMessage(IdentifiedMessage.builder().messageId("messageid").createdDate(
            LocalDateTime.now(ZoneOffset.UTC)).build());

    var currentOpenMessages = liveEventMessageQueue.getCurrentOpenMessages();

    assertThat(currentOpenMessages, hasSize(0));
  }

}
