package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_CONFIGURATION_PROPERTIES;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithConfigurationProperties;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class PropertiesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_PROPERTIES_NAME_MATCHING = ".+Properties";

  PropertiesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public PropertiesConfigurer namesShouldEndWithProperties() {
    return namesShouldMatch(DEFAULT_PROPERTIES_NAME_MATCHING, defaultConfiguration());
  }

  public PropertiesConfigurer namesShouldEndWithProperties(Configuration configuration) {
    return namesShouldMatch(DEFAULT_PROPERTIES_NAME_MATCHING, configuration);
  }

  public PropertiesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  public PropertiesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithConfigurationProperties(true)))
        .should().haveNameMatching(regex)
        .as("Properties should have name ending %s".formatted(regex)), configuration));
  }

  public PropertiesConfigurer shouldBeAnnotatedWithValidated() {
    return shouldBeAnnotatedWithValidated(defaultConfiguration());
  }

  public PropertiesConfigurer shouldBeAnnotatedWithValidated(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithConfigurationProperties(true)))
            .should().beMetaAnnotatedWith(ANNOTATION_VALIDATED)
            .as("Configuration properties annotated with %s should be annotated with %s as well".formatted(
                ANNOTATION_CONFIGURATION_PROPERTIES, ANNOTATION_VALIDATED)),
        configuration));
  }

  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties() {
    return shouldBeAnnotatedWithConfigurationProperties(DEFAULT_PROPERTIES_NAME_MATCHING,
        defaultConfiguration());
  }

  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(
      Configuration configuration) {
    return shouldBeAnnotatedWithConfigurationProperties(DEFAULT_PROPERTIES_NAME_MATCHING,
        configuration);
  }

  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(String regex) {
    return shouldBeAnnotatedWithConfigurationProperties(regex, defaultConfiguration());
  }

  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithConfigurationProperties(true)))
            .as("Configuration properties should be annotated with %s".formatted(
                ANNOTATION_CONFIGURATION_PROPERTIES)),
        configuration));
  }
}
