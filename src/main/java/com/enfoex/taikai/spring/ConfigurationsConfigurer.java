package com.enfoex.taikai.spring;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.Configurers;

public final class ConfigurationsConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".*Configuration";

  public ConfigurationsConfigurer(Configurers configurers) {
    super(configurers);
  }

  public ConfigurationsConfigurer shouldHaveNameEndingConfiguration() {
    shouldHaveNameMatching(DEFAULT_CONFIGURATION_NAME_MATCHING);
    return this;
  }

  public ConfigurationsConfigurer shouldHaveNameMatching(String regex) {
    addRule(classes().that()
        .areAnnotatedWith("org.springframework.context.annotation.Configuration")
        .should().haveNameMatching(regex)
        .as("Configurations should have name ending " + regex));
    return this;
  }
}
