package de.caritas.cob.liveservice.websocket.config;

import static de.caritas.cob.liveservice.websocket.model.Subscription.EVENTS;

import de.caritas.cob.liveservice.websocket.service.ClientInboundChannelInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Global websocket configuration class.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Value("${app.base.url}")
  private String appBaseUrl;

  private final @NonNull ClientInboundChannelInterceptor clientInboundChannelInterceptor;

  /**
   * Configures the socket message broker to provide several endpoints.
   *
   * @param config the message broker registry
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker(EVENTS.getSubscriptionEndpoint())
        .setTaskScheduler(new DefaultManagedTaskScheduler());
  }

  /**
   * Registers the global websocket endpoint.
   *
   * @param registry the stomp endpoint registry
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/live")
        .setAllowedOriginPatterns(this.appBaseUrl)
        .withSockJS();
  }

  /**
   * Configures additional client inbound handling interceptors.
   *
   * @param registration the socket channel registration
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(this.clientInboundChannelInterceptor);
  }

}
