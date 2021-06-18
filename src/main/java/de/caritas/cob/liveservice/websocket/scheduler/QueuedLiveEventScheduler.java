package de.caritas.cob.liveservice.websocket.scheduler;

import de.caritas.cob.liveservice.websocket.service.QueuedLiveEventSendService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to trigger sending of queued live events.
 */
@Component
@RequiredArgsConstructor
public class QueuedLiveEventScheduler {

  private final @NonNull QueuedLiveEventSendService queuedLiveEventSendService;

  /**
   * Triggers the sending of queued live events.
   */
  @Scheduled(cron = "${live.event.retry.send.cron}")
  public void sendQueuedLiveEvents() {
    this.queuedLiveEventSendService.sendQueuedLiveEvents();
  }

}
