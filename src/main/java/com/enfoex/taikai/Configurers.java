package com.enfoex.taikai;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class Configurers implements Iterable<Configurer> {

  private final LinkedHashMap<Class<? extends Configurer>, Configurer> configurers;

  Configurers() {
    this.configurers = new LinkedHashMap<>();
  }

  public <C extends Configurer> C getOrApply(C configurer) {
    Objects.requireNonNull(configurer);
    C existingConfigurer = (C) this.get(configurer.getClass());
    return existingConfigurer != null ? existingConfigurer : this.apply(configurer);
  }

  private <C extends Configurer> C apply(C configurer) {
    this.add(configurer);
    return configurer;
  }

  private <C extends Configurer> void add(C configurer) {
    Class<? extends Configurer> clazz = configurer.getClass();
    this.configurers.putIfAbsent(clazz, configurer);
  }

  public <C extends Configurer> C get(Class<C> clazz) {
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
