package de.caritas.cob.liveservice.exception;

public class InvalidAccessTokenException extends RuntimeException {

  public InvalidAccessTokenException(Exception cause) {
    super(cause);
  }

}
