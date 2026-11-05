package com.enofex.taikai.internal;

import com.enofex.taikai.TaikaiException;
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

  /**
   * Returns the namespace of a rule that cannot do without one.
   *
   * @param configured the namespace of the rule's configuration
   * @param global the namespace of the builder
   * @return the namespace
   * @throws TaikaiException if neither is set
   */
  public static String require(@Nullable String configured, @Nullable String global) {
    String namespace = of(configured, global);

    if (namespace == null) {
      throw new TaikaiException("Namespace is not provided");
    }

    return namespace;
  }
}
