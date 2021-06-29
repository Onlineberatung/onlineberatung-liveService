package de.caritas.cob.liveservice.config.security;

import de.caritas.cob.liveservice.api.config.SpringFoxConfig;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * Configuration class to provide the keycloak security configuration.
 */
@KeycloakConfiguration
public class WebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  /**
   * Configures the basic http security behavior.
   *
   * @param http springs http security
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    /*http
        .csrf().disable()
        .authenticationProvider(keycloakAuthenticationProvider())
        .addFilterBefore(keycloakAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
        .and()
        .authorizeRequests()
        .antMatchers(SpringFoxConfig.WHITE_LIST).permitAll()
        .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/live"))).permitAll()
        .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/live/**")))
        .permitAll();*/
  }

  /**
   * Provides the authentication strategy.
   *
   * @return the configured {@link SessionAuthenticationStrategy}
   */
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  /**
   * Provides the keycloak configuration resolver bean.
   *
   * @return the configured {@link KeycloakConfigResolver}
   */
  @Bean
  public KeycloakConfigResolver keycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

}
