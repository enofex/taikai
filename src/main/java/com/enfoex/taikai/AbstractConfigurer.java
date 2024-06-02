package com.enfoex.taikai;

import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class AbstractConfigurer implements Configurer {

  private final ConfigurerContext configurerContext;
  private final Collection<ArchRule> rules;

  protected AbstractConfigurer(ConfigurerContext configurerContext) {
    Objects.requireNonNull(configurerContext);
    this.configurerContext = configurerContext;
    this.rules = new ArrayList<>();
  }

  public ConfigurerContext configurerContext() {
    return this.configurerContext;
  }

  @Override
  public Collection<ArchRule> rules() {
    return this.rules;
  }

  public boolean addRule(ArchRule rule) {
    return this.rules.add(rule);
  }

  public void disable(Class clazz) {
    if (clazz != null) {
      Configurer configurer = this.configurerContext.configurers().get(clazz);

      if (configurer != null) {
        configurer.disable();
      }
    }
  }
}
