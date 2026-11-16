package com.enofex.taikai.checks;

import java.util.List;

/**
 * Validates the settings of the checks.
 */
final class CheckSettings {

  private CheckSettings() {
  }

  /**
   * Returns an unmodifiable copy of the values.
   *
   * @throws IllegalArgumentException with the given message if there are no values
   */
  static <T> List<T> requireNotEmpty(List<T> values, String message) {
    List<T> copy = List.copyOf(values);

    if (copy.isEmpty()) {
      throw new IllegalArgumentException(message);
    }

    return copy;
  }
}
