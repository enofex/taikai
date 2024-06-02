package com.enfoex.taikai.spring;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.Configurers;

public final class ControllersConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".*Controller";

  public ControllersConfigurer(Configurers configurers) {
    super(configurers);
  }

  public ControllersConfigurer shouldHaveNameEndingController() {
    shouldHaveNameMatching(DEFAULT_CONTROLLER_NAME_MATCHING);
    return this;
  }

  public ControllersConfigurer shouldHaveNameMatching(String regex) {
    addRule(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().haveNameMatching(regex)
        .as("Controllers should have name ending " + regex));
    return this;
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController() {
    shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING);
    return this;
  }

  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex) {
    addRule(classes().that()
        .haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .as("Controllers should be annotated with @RestController"));
    return this;
  }

  public ControllersConfigurer shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ControllersConfigurer shouldBeAnnotatedWithController(String regex) {
    addRule(classes().that().haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .as("Controllers should be annotated with @Controller"));
    return this;
  }

  public ControllersConfigurer shouldBePackagePrivate() {
    addRule(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().bePackagePrivate()
        .as("Controllers should be package-private"));
    return this;
  }

  public ControllersConfigurer shouldNotDependOnOtherController() {
    addRule(classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should(not(dependOnClassesThat(
            annotatedWith("org.springframework.web.bind.annotation.RestController")
                .or(annotatedWith("org.springframework.web.bind.annotation.Controller")))))
        .as("Controllers should not be depend on other Controller"));
    return this;
  }
}
