package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringPredicates.metaAnnotatedWithConfiguration;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class ConfigurationsConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".*Configuration";

  ConfigurationsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ConfigurationsConfigurer namesShouldEndWithConfiguration() {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, null);
  }

  public ConfigurationsConfigurer namesShouldEndWithConfiguration(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, configuration);
  }

  public ConfigurationsConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, null);
  }

  public ConfigurationsConfigurer namesShouldMatch(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(metaAnnotatedWithConfiguration()))
        .should().haveNameMatching(regex)
        .as("Configurations should have name ending %s".formatted(regex)), configuration));
  }
}
