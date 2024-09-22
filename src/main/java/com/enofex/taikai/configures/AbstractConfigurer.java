package com.enofex.taikai.configures;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public abstract class AbstractConfigurer implements Configurer {

  private final ConfigurerContext configurerContext;
  private final Collection<TaikaiRule> rules;

  protected AbstractConfigurer(ConfigurerContext configurerContext) {
    this.configurerContext = requireNonNull(configurerContext);
    this.rules = new ArrayList<>();
  }

  protected ConfigurerContext configurerContext() {
    return this.configurerContext;
  }

  protected <T extends Configurer> T addRule(TaikaiRule rule) {
    this.rules.add(rule);
    return (T) this;
  }

  protected <T extends Configurer> void disable(Class<T> clazz) {
    if (clazz != null) {
      Configurer configurer = this.configurerContext.configurers().get(clazz);

      if (configurer != null) {
        configurer.disable();
      }
    }
  }

  protected <T extends Configurer, C extends Configurer> C customizer(Customizer<T> customizer,
      Supplier<T> supplier) {
    requireNonNull(customizer);
    requireNonNull(supplier);

    customizer.customize(this.configurerContext
        .configurers()
        .getOrApply(supplier.get()));

    return (C) this;
  }

  @Override
  public Collection<TaikaiRule> rules() {
    return this.rules;
  }
}
