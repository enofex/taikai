package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REST_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRestController;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class ControllersConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".+Controller";

  ControllersConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ControllersConfigurer namesShouldEndWithController() {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, Configuration.defaultConfiguration());
  }

  public ControllersConfigurer namesShouldEndWithController(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  public ControllersConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, Configuration.defaultConfiguration());
  }

  public ControllersConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should().haveNameMatching(regex)
        .as("Controllers should have name ending %s".formatted(regex)), configuration));
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController() {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING,
        Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController(Configuration configuration) {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex) {
    return shouldBeAnnotatedWithRestController(regex, Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithRestController(true)))
            .as("Controllers should be annotated with %s".formatted(ANNOTATION_REST_CONTROLLER)),
        configuration));
  }

  public ControllersConfigurer shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING,
        Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldBeAnnotatedWithController(Configuration configuration) {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  public ControllersConfigurer shouldBeAnnotatedWithController(String regex) {
    return shouldBeAnnotatedWithController(regex, Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldBeAnnotatedWithController(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithController(true)))
            .as("Controllers should be annotated with %s".formatted(ANNOTATION_CONTROLLER)),
        configuration));
  }

  public ControllersConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should().bePackagePrivate()
        .as("Controllers should be package-private"), configuration));
  }

  public ControllersConfigurer shouldNotDependOnOtherControllers() {
    return shouldNotDependOnOtherControllers(Configuration.defaultConfiguration());
  }

  public ControllersConfigurer shouldNotDependOnOtherControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should(not(dependOnClassesThat(are(annotatedWithControllerOrRestController(true)))))
        .as("Controllers should not be depend on other Controllers"), configuration));
  }
}
