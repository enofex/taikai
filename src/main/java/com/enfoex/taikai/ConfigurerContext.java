package com.enfoex.taikai;

public final class ConfigurerContext {

  private final Configurers configurers;

  public ConfigurerContext(Configurers configurers) {
    this.configurers = configurers;
  }

  public Configurers configurers() {
    return this.configurers;
  }
}
