package com.enofex.taikai.configures;

import org.jspecify.annotations.Nullable;

public final class ConfigurerContext {

  @Nullable private final String namespace;
  private final Configurers configurers;

  public ConfigurerContext(@Nullable String namespace, Configurers configurers) {
    this.namespace = namespace;
    this.configurers = configurers;
  }

  public String namespace() {
    return this.namespace;
  }

  public Configurers configurers() {
    return this.configurers;
  }
}
