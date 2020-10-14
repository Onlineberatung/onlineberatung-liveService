package de.caritas.cob.liveservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starter class for the application.
 */
@SpringBootApplication
public class LiveServiceApplication {

  /**
   * Global application entry point.
   *
   * @param args possible provided args
   */
  public static void main(String[] args) {
    SpringApplication.run(LiveServiceApplication.class, args);
  }

}
