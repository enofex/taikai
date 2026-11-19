package com.enofex.taikai.internal;

import org.jspecify.annotations.Nullable;

/**
 * Resolves the namespace of a rule: its own namespace if it is set, otherwise the namespace of the
 * builder.
 */
public final class Namespaces {

  private Namespaces() {
  }

  /**
   * Returns the namespace of a rule.
   *
   * @param configured the namespace of the rule's configuration
   * @param global the namespace of the builder
   * @return the namespace, or {@code null} if neither is set
   */
  public static @Nullable String of(@Nullable String configured, @Nullable String global) {
    return configured != null ? configured : global;
  }
}
