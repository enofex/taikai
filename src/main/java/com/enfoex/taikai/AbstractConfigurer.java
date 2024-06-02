package com.enfoex.taikai;

import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class AbstractConfigurer implements Configurer {

  private final Configurers configurers;
  private final Collection<ArchRule> rules;

  protected AbstractConfigurer(Configurers configurers) {
    Objects.requireNonNull(configurers);
    this.configurers = configurers;
    this.rules = new ArrayList<>();
  }

  public Configurers configurers() {
    return this.configurers;
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
      Configurer configurer = configurers().get(clazz);

      if (configurer != null) {
        configurer.disable();
      }
    }
  }
}
