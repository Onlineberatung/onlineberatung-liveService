package de.caritas.cob.liveservice.websocket.model;

import de.caritas.cob.liveservice.api.model.LiveEventMessage;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class IdentifiedMessage {

  private String messageId;
  private WebSocketUserSession websocketUserSession;
  private LiveEventMessage liveEventMessage;
  private Integer retryAmount;
  private LocalDateTime createdDate;

}
