package com.enfoex.taikai;

@FunctionalInterface
public interface Customizer<T> {

  Customizer DISABLED = t -> {
  };

  Customizer DEFAULTS = t -> {
  };

  void customize(T t);

  static Customizer defaults() {
    return DEFAULTS;
  }

  static Customizer disable() {
    return DISABLED;
  }
}

