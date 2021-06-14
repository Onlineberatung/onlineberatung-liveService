package de.caritas.cob.liveservice.websocket.registry;

import de.caritas.cob.liveservice.websocket.model.IdentifiedMessage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Container class to hold all queued live event message as long as they are acknowledged by the
 * client.
 */
@Component
public class LiveEventMessageQueue {

  private static final Logger LOGGER = LoggerFactory.getLogger(LiveEventMessageQueue.class);
  private static final Set<IdentifiedMessage> QUEUED_LIVE_MESSAGES = new CopyOnWriteArraySet<>();

  /**
   * Adds the given {@link IdentifiedMessage} to the registry.
   *
   * @param identifiedMessage the identified queued message
   */
  public synchronized void addIdentifiedMessage(IdentifiedMessage identifiedMessage) {
    LOGGER.info("Add message with id {} to queue", identifiedMessage.getMessageId());
    QUEUED_LIVE_MESSAGES.add(identifiedMessage);
  }

  /**
   * Removes the {@link IdentifiedMessage} with the given id.
   *
   * @param messageId the id of the {@link IdentifiedMessage}
   */
  public synchronized void removeIdentifiedMessageWithId(String messageId) {
    LOGGER.info("Remove message with id {} from queue", messageId);
    QUEUED_LIVE_MESSAGES.removeIf(message -> message.getMessageId().equals(messageId));
  }

  /**
   * Retrieves all current queued {@link IdentifiedMessage}s.
   *
   * @return all current {@link IdentifiedMessage}
   */
  public synchronized Collection<IdentifiedMessage> getCurrentOpenMessages() {
    return new LinkedList<>(QUEUED_LIVE_MESSAGES);
  }

}
