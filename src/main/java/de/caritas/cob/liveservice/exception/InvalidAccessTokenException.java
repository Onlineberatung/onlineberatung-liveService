package de.caritas.cob.liveservice.exception;

/**
 * Exception for authentication errors.
 */
public class InvalidAccessTokenException extends RuntimeException {

  /**
   * Constructor to create a new {@link InvalidAccessTokenException}.
   *
   * @param cause the cause to wrap
   */
  public InvalidAccessTokenException(Exception cause) {
    super(cause);
  }

}
