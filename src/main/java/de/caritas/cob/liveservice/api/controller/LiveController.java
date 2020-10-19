package de.caritas.cob.liveservice.api.controller;

import de.caritas.cob.liveservice.api.model.EventType;
import de.caritas.cob.liveservice.generated.api.controller.LiveeventApi;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for triggering live events.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "live-controller")
public class LiveController implements LiveeventApi {

  /**
   * Trigger entry point for live event sending.
   *
   * @param userIds the ids of the users to send a live event
   * @param eventType the {@link EventType} of the live event
   */
  @Override
  public ResponseEntity<Void> sendLiveEvent(@Valid @RequestParam List<String> userIds,
      @Valid @RequestParam String eventType) {
    EventType.fromValue(eventType);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
