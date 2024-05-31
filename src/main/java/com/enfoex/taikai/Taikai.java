package com.enfoex.taikai;

import com.enfoex.taikai.spring.SpringCustomizer;
import java.util.LinkedHashMap;

public final class Taikai {

  private Taikai() {
  }

  public static TaikaiBuilder builder() {
    return new TaikaiBuilder();
  }

  public static final class TaikaiBuilder {

    private final LinkedHashMap<Class<? extends Customizer<TaikaiBuilder>>, Customizer<TaikaiBuilder>> customizers;

    TaikaiBuilder() {
      this.customizers = new LinkedHashMap<>();
    }

    public TaikaiBuilder spring(Customizer<SpringCustomizer> customizer) {
      customizer.customize(this.getOrApply(new SpringCustomizer()));
      return this;
    }

    private <C extends Customizer<TaikaiBuilder>> C getOrApply(C customizer) {
      C existingCustomizer = (C) this.getCustomizer(customizer.getClass());
      return existingCustomizer != null ? existingCustomizer : this.apply(customizer);
    }

    private <C extends Customizer<TaikaiBuilder>> C apply(C customizer) {
      this.addCustomizer(customizer);
      return customizer;
    }

    private <C extends Customizer<TaikaiBuilder>> void addCustomizer(C customizer) {
      Class clazz = customizer.getClass();
      this.customizers.putIfAbsent(clazz, customizer);
    }

    private <C extends Customizer<TaikaiBuilder>> C getCustomizer(Class<C> clazz) {
      return (C) this.customizers.get(clazz);
    }

    private <C extends Customizer<TaikaiBuilder>> C removeCustomizer(Class<C> clazz) {
      return (C) this.customizers.remove(clazz);
    }

    public Taikai build() {
      return new Taikai();
    }
  }
}
