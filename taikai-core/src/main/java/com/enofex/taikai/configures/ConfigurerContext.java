package com.enofex.taikai.configures;

import org.jspecify.annotations.Nullable;

/**
 * The context that the configurers of a builder share: its namespace and its configurers.
 */
public final class ConfigurerContext {

  private final @Nullable String namespace;
  private final Configurers configurers;

  /**
   * Creates the context.
   *
   * @param namespace the namespace of the builder, or {@code null} if none is set
   * @param configurers the configurers of the builder
   */
  public ConfigurerContext(@Nullable String namespace, Configurers configurers) {
    this.namespace = namespace;
    this.configurers = configurers;
  }

  /**
   * Returns the namespace of the builder.
   *
   * @return the namespace, or {@code null} if none is set
   */
  public @Nullable String namespace() {
    return this.namespace;
  }

  /**
   * Returns the configurers of the builder.
   *
   * @return the configurers
   */
  public Configurers configurers() {
    return this.configurers;
  }
}
