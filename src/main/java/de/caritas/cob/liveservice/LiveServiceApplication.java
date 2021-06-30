package de.caritas.cob.liveservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Starter class for the application.
 */
@EnableScheduling
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
