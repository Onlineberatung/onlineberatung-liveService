package de.caritas.cob.liveservice.config.security;

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.HEARTBEAT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Configuration class for websocket security.
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

  /**
   * Configures the security handling for incoming socket messages.
   *
   * @param messages the messages to be handled
   */
  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    messages
        .simpDestMatchers("/live/**").authenticated()
        .simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll()
        .anyMessage().permitAll();
  }

  /**
   * Decides if same origin should be disabled.
   *
   * @return true
   */
  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }

}
