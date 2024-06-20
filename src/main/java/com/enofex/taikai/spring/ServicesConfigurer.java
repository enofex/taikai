package com.enofex.taikai.spring;

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

public final class ServicesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_SERVICE_NAME_MATCHING = ".+Service";

  ServicesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ServicesConfigurer namesShouldEndWithService() {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, null);
  }

  public ServicesConfigurer namesShouldEndWithService(Configuration configuration) {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  public ServicesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, null);
  }

  public ServicesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithService(true)))
        .should().haveNameMatching(regex)
        .as("Services should have name ending %s".formatted(regex)), configuration));
  }

  public ServicesConfigurer shouldBeAnnotatedWithService() {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, null);
  }

  public ServicesConfigurer shouldBeAnnotatedWithService(Configuration configuration) {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  public ServicesConfigurer shouldBeAnnotatedWithService(String regex) {
    return shouldBeAnnotatedWithService(regex, null);
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
    return shouldNotDependOnControllers(null);
  }

  public ServicesConfigurer shouldNotDependOnControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithService(true)))
            .should(not(dependOnClassesThat(annotatedWithControllerOrRestController(true))))
            .as("Services should not depend on Controllers or RestControllers"),
        configuration));
  }
}

