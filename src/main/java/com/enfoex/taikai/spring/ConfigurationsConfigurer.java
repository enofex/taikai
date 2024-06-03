package com.enfoex.taikai.spring;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.TaikaiRule;

public final class ConfigurationsConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".*Configuration";

  public ConfigurationsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ConfigurationsConfigurer shouldHaveNameEndingConfiguration() {
    return shouldHaveNameMatching(DEFAULT_CONFIGURATION_NAME_MATCHING);
  }

  public ConfigurationsConfigurer shouldHaveNameMatching(String regex) {
    return addRule(TaikaiRule.of(classes().that()
        .areAnnotatedWith("org.springframework.context.annotation.Configuration")
        .should().haveNameMatching(regex)
        .as("Configurations should have name ending " + regex)));
  }
}
