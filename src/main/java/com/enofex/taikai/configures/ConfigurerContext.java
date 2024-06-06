package com.enofex.taikai.configures;

public final class ConfigurerContext {

  private final String namespace;
  private final Configurers configurers;

  public ConfigurerContext(String namespace, Configurers configurers) {
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
