package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SERVICE;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithService;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

public class ServicesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_SERVICE_NAME_MATCHING = ".+Service";

  ServicesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ServicesConfigurer namesShouldEndWithService() {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, defaultConfiguration());
  }

  public ServicesConfigurer namesShouldEndWithService(Configuration configuration) {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  public ServicesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  public ServicesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithService(true)))
        .should().haveNameMatching(regex)
        .as("Services should have name ending %s".formatted(regex)), configuration));
  }

  public ServicesConfigurer shouldBeAnnotatedWithService() {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, defaultConfiguration());
  }

  public ServicesConfigurer shouldBeAnnotatedWithService(Configuration configuration) {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  public ServicesConfigurer shouldBeAnnotatedWithService(String regex) {
    return shouldBeAnnotatedWithService(regex, defaultConfiguration());
  }

  public ServicesConfigurer shouldBeAnnotatedWithService(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithService(true)))
            .as("Services should be annotated with %s".formatted(ANNOTATION_SERVICE)),
        configuration));
  }

  public ServicesConfigurer shouldNotDependOnControllers() {
    return shouldNotDependOnControllers(defaultConfiguration());
  }

  public ServicesConfigurer shouldNotDependOnControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithService(true)))
            .should(not(dependOnClassesThat(annotatedWithControllerOrRestController(true))))
            .as("Services should not depend on Controllers or RestControllers"),
        configuration));
  }

  public static final class Disableable extends ServicesConfigurer implements
      DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public ServicesConfigurer disable() {
      disable(ServicesConfigurer.class);

      return this;
    }
  }
}

