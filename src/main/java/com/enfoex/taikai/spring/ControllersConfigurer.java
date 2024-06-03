package com.enfoex.taikai.spring;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.TaikaiRule;

public final class ControllersConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".*Controller";

  public ControllersConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ControllersConfigurer shouldHaveNameEndingController() {
    return shouldHaveNameMatching(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ControllersConfigurer shouldHaveNameMatching(String regex) {
    return addRule(TaikaiRule.of(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().haveNameMatching(regex)
        .as("Controllers should have name ending " + regex)));
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController() {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex) {
    return addRule(TaikaiRule.of(classes().that()
        .haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .as("Controllers should be annotated with @RestController")));
  }

  public ControllersConfigurer shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ControllersConfigurer shouldBeAnnotatedWithController(String regex) {
    return addRule(TaikaiRule.of(classes().that().haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .as("Controllers should be annotated with @Controller")));
  }

  public ControllersConfigurer shouldBePackagePrivate() {
    return addRule(TaikaiRule.of(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().bePackagePrivate()
        .as("Controllers should be package-private")));
  }

  public ControllersConfigurer shouldNotDependOnOtherController() {
    return addRule(TaikaiRule.of(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should(not(dependOnClassesThat(
            annotatedWith("org.springframework.web.bind.annotation.RestController")
                .or(annotatedWith("org.springframework.web.bind.annotation.Controller")))))
        .as("Controllers should not be depend on other Controller")));
  }
}
