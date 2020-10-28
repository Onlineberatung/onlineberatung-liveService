package de.caritas.cob.liveservice.websocket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import de.caritas.cob.liveservice.websocket.model.WebSocketUserSession;
import org.junit.Before;
import org.junit.Test;

public class SocketUserRegistryTest {

  private final SocketUserRegistry socketUserRegistry = new SocketUserRegistry();

  @Before
  public void cleanup() {
    socketUserRegistry.retrieveAllUsers()
        .forEach(socketUser -> socketUserRegistry.removeSession(socketUser.getWebsocketSessionId()));
  }

  @Test
  public void addUser_Should_addUserToRegistry() {
    socketUserRegistry.addUser(WebSocketUserSession.builder().websocketSessionId("userId").build());

    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(1));
  }

  @Test
  public void removeSession_Should_removeSession_When_sessionExists() {
    socketUserRegistry
        .addUser(WebSocketUserSession.builder().websocketSessionId("session").build());

    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(1));
    socketUserRegistry.removeSession("session");
    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(0));
  }

  @Test
  public void removeSession_Should_notRemoveSession_When_sessionDoesNotExists() {
    socketUserRegistry
        .addUser(WebSocketUserSession.builder().websocketSessionId("session").build());

    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(1));
    socketUserRegistry.removeSession("other");
    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(1));
  }

  @Test
  public void findUserBySessionId_Should_returnUser_When_sessionUserExists() {
    socketUserRegistry
        .addUser(WebSocketUserSession.builder().websocketSessionId("session").build());

    WebSocketUserSession userBySession = socketUserRegistry.findUserBySessionId("session");

    assertThat(userBySession, notNullValue());
    assertThat(userBySession.getWebsocketSessionId(), is("session"));
  }

  @Test
  public void findUserBySessionId_Should_returnNull_When_sessionDoesNotExists() {
    socketUserRegistry
        .addUser(WebSocketUserSession.builder().websocketSessionId("session").build());

    WebSocketUserSession userBySession = socketUserRegistry.findUserBySessionId("other");

    assertThat(userBySession, nullValue());
  }

  @Test
  public void retrieveAllUsers_Should_alwaysReturnAllRegisteredUsers() {
    for (int incrementer = 0; incrementer < 5000; incrementer++) {
      socketUserRegistry.addUser(
          WebSocketUserSession.builder()
              .websocketSessionId("user " + incrementer)
              .build());
    }

    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(5000));
    for (int incrementer = 0; incrementer < 2500; incrementer++) {
      socketUserRegistry.removeSession("user " + incrementer);
    }

    assertThat(socketUserRegistry.retrieveAllUsers(), hasSize(2500));
    WebSocketUserSession webSocketUserSession = socketUserRegistry.retrieveAllUsers().get(0);
    assertThat(webSocketUserSession.getWebsocketSessionId(), is("user 2500"));
  }

}
