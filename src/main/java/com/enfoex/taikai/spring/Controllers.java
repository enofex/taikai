package com.enfoex.taikai.spring;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.lang.ArchRule;

public final class Controllers {

  public static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".*Controller";

  Controllers() {
  }

  public ArchRule shouldHaveNameEndingController() {
    return shouldHaveNameMatching(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ArchRule shouldHaveNameMatching(String regex) {
    return classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().haveNameMatching(regex)
        .as("Controllers should have name ending " + regex);
  }

  public ArchRule shouldBeAnnotatedWithRestController() {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ArchRule shouldBeAnnotatedWithRestController(String regex) {
    return classes().that()
        .haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .as("Controllers should be annotated with @RestController");
  }

  public ArchRule shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING);
  }

  public ArchRule shouldBeAnnotatedWithController(String regex) {
    return classes().that().haveNameMatching(regex)
        .should()
        .beAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .as("Controllers should be annotated with @Controller");
  }

  public ArchRule shouldBePackagePrivate() {
    return classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should().bePackagePrivate()
        .as("Controllers should be package-private");
  }

  public ArchRule shouldNotDependOnOtherController() {
    return classes().that()
        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .or()
        .areAnnotatedWith("org.springframework.web.bind.annotation.Controller")
        .should(not(dependOnClassesThat(
            annotatedWith("org.springframework.web.bind.annotation.RestController")
                .or(annotatedWith("org.springframework.web.bind.annotation.Controller")))))
        .as("Controllers should not be depend on other Controller");
  }
}
