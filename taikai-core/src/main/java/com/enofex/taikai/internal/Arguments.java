package com.enofex.taikai.internal;

import java.util.List;

/**
 * Validates the arguments that the settings of checks and exports share.
 */
public final class Arguments {

  private Arguments() {
  }

  /**
   * Returns an unmodifiable copy of the values.
   *
   * @param values the values
   * @param message the message of the exception if there are no values
   * @param <T> the type of the values
   * @return the copy of the values
   * @throws IllegalArgumentException with the given message if there are no values
   */
  public static <T> List<T> requireNotEmpty(List<T> values, String message) {
    List<T> copy = List.copyOf(values);

    if (copy.isEmpty()) {
      throw new IllegalArgumentException(message);
    }

    return copy;
  }
}
