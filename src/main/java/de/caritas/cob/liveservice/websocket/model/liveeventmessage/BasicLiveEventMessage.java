package de.caritas.cob.liveservice.websocket.model.liveeventmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicLiveEventMessage implements LiveEventMessage {

  @JsonProperty
  private String eventType;

}
