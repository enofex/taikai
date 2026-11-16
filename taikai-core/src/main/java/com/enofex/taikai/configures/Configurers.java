package com.enofex.taikai.configures;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The configurers of a builder, at most one of each type, in the order of their registration.
 */
public final class Configurers implements Iterable<Configurer> {

  private final Map<Class<? extends Configurer>, Configurer> configurers;

  /**
   * Creates an empty registry.
   */
  public Configurers() {
    this.configurers = new LinkedHashMap<>();
  }

  /**
   * Registers the configurer unless one of its type is registered already, and returns the
   * registered one, so that repeated calls customize the same configurer.
   *
   * @param configurer the configurer to register
   * @param <C> the type of the configurer
   * @return the registered configurer of that type
   */
  public <C extends Configurer> C getOrApply(C configurer) {
    requireNonNull(configurer);

    return (C) this.configurers.computeIfAbsent(configurer.getClass(), key -> configurer);
  }

  /**
   * Returns the registered configurer of the given type.
   *
   * @param clazz the type of the configurer
   * @param <C> the type of the configurer
   * @return the configurer, or {@code null} if none of that type is registered
   */
  public <C extends Configurer> @Nullable C get(Class<C> clazz) {
    return (C) this.configurers.get(clazz);
  }

  /**
   * Returns all registered configurers.
   *
   * @return the configurers, in the order of their registration
   */
  public Collection<Configurer> all() {
    return this.configurers.values();
  }

  @Override
  public Iterator<Configurer> iterator() {
    return this.configurers.values().iterator();
  }
}
