package com.enofex.taikai;

/**
 * Thrown when Taikai is configured incorrectly, for example when a rule needs a namespace and none
 * is provided.
 */
public final class TaikaiException extends RuntimeException {

  /**
   * Creates an exception with the given message.
   *
   * @param message the detail message
   */
  public TaikaiException(String message) {
    super(message);
  }

  /**
   * Creates an exception with the given message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public TaikaiException(String message, Throwable cause) {
    super(message, cause);
  }
}
