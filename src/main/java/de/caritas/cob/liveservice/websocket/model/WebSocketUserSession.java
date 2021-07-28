package de.caritas.cob.liveservice.websocket.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents one socket user connection.
 */
@Data
@Builder
@EqualsAndHashCode
public class WebSocketUserSession {

  private String websocketSessionId;
  private String subscriptionId;
  private String userId;

}
