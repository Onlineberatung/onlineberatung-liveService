package de.caritas.cob.liveservice.websocket.model;

import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentifiedMessage {

  private String messageId;
  private WebSocketUserSession websocketUserSession;
  private LiveEventMessage liveEventMessage;
  private Integer retryAmount;

}
