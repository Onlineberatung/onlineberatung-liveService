package de.caritas.cob.liveservice.websocket.registry;

import static java.time.temporal.ChronoUnit.SECONDS;

import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Container class to hold all queued live event message as long as they are acknowledged by the
 * client.
 */
@Component
public class LiveEventMessageQueue {

  private static final Logger LOGGER = LoggerFactory.getLogger(LiveEventMessageQueue.class);

  private final Set<IdentifiedMessage> queuedLiveMessages = new CopyOnWriteArraySet<>();

  @Value("${live.event.minimum.seconds.before.retry}")
  private Integer minimumSecondsBeforeRetry;

  /**
   * Adds the given {@link IdentifiedMessage} to the registry.
   *
   * @param identifiedMessage the identified queued message
   */
  public synchronized void addIdentifiedMessage(IdentifiedMessage identifiedMessage) {
    LOGGER.info("Add message with id {} to queue", identifiedMessage.getMessageId());
    this.queuedLiveMessages.add(identifiedMessage);
  }

  /**
   * Removes the {@link IdentifiedMessage} with the given id.
   *
   * @param messageId the id of the {@link IdentifiedMessage}
   */
  public synchronized void removeIdentifiedMessageWithId(String messageId) {
    LOGGER.info("Remove message with id {} from queue", messageId);
    this.queuedLiveMessages.removeIf(message -> message.getMessageId().equals(messageId));
  }

  /**
   * Retrieves all current queued {@link IdentifiedMessage}s.
   *
   * @return all current {@link IdentifiedMessage}
   */
  public synchronized Collection<IdentifiedMessage> getCurrentOpenMessages() {
    return new LinkedList<>(queuedLiveMessages).stream()
        .filter(this::minimumSecondsBeforeRetryReached)
        .collect(Collectors.toList());
  }

  private boolean minimumSecondsBeforeRetryReached(IdentifiedMessage identifiedMessage) {
    return identifiedMessage.getCreatedDate()
        .plus(this.minimumSecondsBeforeRetry, SECONDS)
        .isBefore(LocalDateTime.now(ZoneOffset.UTC));
  }

}
