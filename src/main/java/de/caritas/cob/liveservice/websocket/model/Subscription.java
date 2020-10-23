package de.caritas.cob.liveservice.websocket.model;

/**
 * Enum to hold constants of subscription endpoints.
 */
public enum Subscription {

  EVENTS("/events");

  private final String subscriptionEndpoint;

  Subscription(String subscriptionEndpoint) {
    this.subscriptionEndpoint = subscriptionEndpoint;
  }

  /**
   * Returns the endpoint prefix.
   *
   * @return the subscription endpoint prefix
   */
  public String getSubscriptionEndpoint() {
    return this.subscriptionEndpoint;
  }

}
