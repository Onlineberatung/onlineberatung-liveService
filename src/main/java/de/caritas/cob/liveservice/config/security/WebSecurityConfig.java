package de.caritas.cob.liveservice.config.security;

import com.google.common.collect.Lists;
import de.caritas.cob.liveservice.api.config.SpringFoxConfig;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    http
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
        .permitAll();
  }

  @Bean
  public FilterRegistrationBean corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration().setAllowedOriginPatterns(Lists.newArrayList("*")).applyPermitDefaultValues();
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
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
