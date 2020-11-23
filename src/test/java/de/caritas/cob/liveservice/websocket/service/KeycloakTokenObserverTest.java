package de.caritas.cob.liveservice.websocket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AdapterTokenVerifier.class, KeycloakDeploymentBuilder.class})
public class KeycloakTokenObserverTest {

  @InjectMocks
  private KeycloakTokenObserver keycloakTokenObserver;

  @Mock
  private KeycloakSpringBootProperties keycloakSpringBootProperties;

  @Test(expected = VerificationException.class)
  public void observeUserId_Should_throwVerificationException_When_tokenIsNull()
      throws VerificationException {
    this.keycloakTokenObserver.observeUserId(null);
  }

  @Test(expected = VerificationException.class)
  public void observeUserId_Should_throwVerificationException_When_tokenIsEmpty()
      throws VerificationException {
    this.keycloakTokenObserver.observeUserId("");
  }

  @Test
  public void observeUserId_Should_returnUserId_When_tokenIsValid()
      throws VerificationException {
    mockStatic(KeycloakDeploymentBuilder.class);
    mockStatic(AdapterTokenVerifier.class);
    AccessToken accessToken = new AccessToken();
    accessToken.setOtherClaims("userId", "validId");
    when(AdapterTokenVerifier.verifyToken(any(), any())).thenReturn(accessToken);

    String userId = this.keycloakTokenObserver.observeUserId("valid token");
    assertThat(userId, is("validId"));
  }

}
