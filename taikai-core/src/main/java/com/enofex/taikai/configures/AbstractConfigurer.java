package com.enofex.taikai.configures;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Base class of the configurers, which keeps their rules and the context of the builder.
 */
public abstract class AbstractConfigurer implements Configurer {

  private final ConfigurerContext configurerContext;
  private final Collection<TaikaiRule> rules;

  /**
   * Creates a configurer without rules.
   *
   * @param configurerContext the context of the builder
   */
  protected AbstractConfigurer(ConfigurerContext configurerContext) {
    this.configurerContext = requireNonNull(configurerContext);
    this.rules = new ArrayList<>();
  }

  /**
   * Returns the context of the builder, with its namespace and configurers.
   *
   * @return the context of the builder
   */
  protected ConfigurerContext configurerContext() {
    return this.configurerContext;
  }

  /**
   * Adds a rule to this configurer.
   *
   * @param rule the rule
   * @param <T> the type of this configurer
   * @return this configurer for fluent chaining
   */
  protected <T extends Configurer> T addRule(TaikaiRule rule) {
    requireNonNull(rule);

    this.rules.add(rule);
    return (T) this;
  }

  /**
   * Removes the rules of the configurer of the given type, if the builder has one.
   *
   * @param clazz the type of the configurer
   * @param <T> the type of the configurer
   */
  protected <T extends Configurer> void disable(Class<T> clazz) {
    requireNonNull(clazz);

    Configurer configurer = this.configurerContext.configurers().get(clazz);

    if (configurer != null) {
      configurer.clear();
    }
  }

  /**
   * Customizes the configurer of the builder that has the type of the supplied one, which is
   * registered first if the builder has none of that type yet.
   *
   * @param customizer the customization of the configurer
   * @param supplier the supplier of a new configurer
   * @param <T> the type of the customized configurer
   * @param <C> the type of this configurer
   * @return this configurer for fluent chaining
   */
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
