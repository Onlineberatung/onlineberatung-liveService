package de.caritas.cob.liveservice.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.keycloak.adapters.rotation.AdapterRSATokenVerifier.verifyToken;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakTokenObserver {

  private static final String KEYCLOAK_USER_ID = "userId";

  private final @NonNull KeycloakSpringBootProperties keyCloakConfiguration;

  public String observeUserId(String token) throws VerificationException {
    if (isBlank(token)) {
      throw new VerificationException("Access token must not be null");
    }
    AccessToken accessToken = verifyToken(token,
        KeycloakDeploymentBuilder.build(this.keyCloakConfiguration));
    return accessToken.getOtherClaims().get(KEYCLOAK_USER_ID).toString();
  }

}
