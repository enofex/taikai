package com.enfoex.taikai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractConfigurer implements Configurer {

  private final ConfigurerContext configurerContext;
  private final Collection<TaikaiRule> rules;

  protected AbstractConfigurer(ConfigurerContext configurerContext) {
    this.configurerContext =  Objects.requireNonNull(configurerContext);;
    this.rules = new ArrayList<>();
  }

  public ConfigurerContext configurerContext() {
    return this.configurerContext;
  }

  @Override
  public Collection<TaikaiRule> rules() {
    return this.rules;
  }

  public <T> T addRule(TaikaiRule rule) {
    this.rules.add(rule);
    return (T) this;
  }

  public <T extends Configurer> void disable(Class<T> clazz) {
    if (clazz != null) {
      Configurer configurer = this.configurerContext.configurers().get(clazz);

      if (configurer != null) {
        configurer.disable();
      }
    }
  }

  public <T extends Configurer> void customizer(Customizer<T> customizer, Supplier<T> supplier) {
    Objects.requireNonNull(customizer);
    Objects.requireNonNull(supplier);
    customizer.customize(this.configurerContext
        .configurers()
        .getOrApply(supplier.get()));
  }
}
