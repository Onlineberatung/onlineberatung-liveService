package de.caritas.cob.liveservice.exception;

/**
 * Exception for authentication errors.
 */
public class InvalidAccessTokenException extends RuntimeException {

  /**
   * Constructor to create a new {@link InvalidAccessTokenException}.
   *
   * @param message the error message
   */
  public InvalidAccessTokenException(String message) {
    super(message);
  }

}
