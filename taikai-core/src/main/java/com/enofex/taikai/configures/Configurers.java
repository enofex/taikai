package com.enofex.taikai.configures;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class Configurers implements Iterable<Configurer> {

  private final Map<Class<? extends Configurer>, Configurer> configurers;

  public Configurers() {
    this.configurers = new LinkedHashMap<>();
  }

  public <C extends Configurer> C getOrApply(C configurer) {
    requireNonNull(configurer);

    return (C) this.configurers.computeIfAbsent(configurer.getClass(), key -> configurer);
  }

  public <C extends Configurer> @Nullable C get(Class<C> clazz) {
    return (C) this.configurers.get(clazz);
  }

  public Collection<Configurer> all() {
    return this.configurers.values();
  }

  @Override
  public Iterator<Configurer> iterator() {
    return this.configurers.values().iterator();
  }
}
