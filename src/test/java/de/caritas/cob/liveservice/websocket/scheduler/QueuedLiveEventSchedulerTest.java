package de.caritas.cob.liveservice.websocket.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.liveservice.websocket.service.QueuedLiveEventSendService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueuedLiveEventSchedulerTest {

  @InjectMocks
  private QueuedLiveEventScheduler queuedLiveEventScheduler;

  @Mock
  private QueuedLiveEventSendService queuedLiveEventSendService;

  @Test
  void sendQueuedLiveEvents_Should_triggerQueuedLiveEventSendservice() {
    this.queuedLiveEventScheduler.sendQueuedLiveEvents();

    verify(this.queuedLiveEventSendService, times(1)).sendQueuedLiveEvents();
  }

}
