package de.caritas.cob.liveservice.websocket.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents one socket user connection.
 */
@Data
@Builder
public class WebSocketUserSession {

  private String websocketSessionId;
  private String subscriptionId;
  private String userId;

}
