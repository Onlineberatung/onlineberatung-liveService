package de.caritas.cob.liveservice.api.controller;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.liveservice.api.facade.LiveEventFacade;
import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import de.caritas.cob.liveservice.generated.api.controller.LiveeventApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for triggering live events.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "live-controller")
public class LiveController implements LiveeventApi {

  private final @NonNull LiveEventFacade liveEventFacade;

  /**
   * Trigger entry point for live event sending.
   *
   * @param liveEventMessage the {@link LiveEventMessage} of the live event
   */
  @Override
  public ResponseEntity<Void> sendLiveEvent(@Valid @RequestBody LiveEventMessage liveEventMessage) {
    if (isEmpty(liveEventMessage.getUserIds())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ids must not be empty");
    }
    this.liveEventFacade.triggerLiveEvent(liveEventMessage);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
