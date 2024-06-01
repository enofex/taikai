package com.enfoex.taikai.spring;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.lang.ArchRule;

public final class Configurations {

  public static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".*Configuration";

  Configurations() {
  }

  public ArchRule shouldHaveNameEndingConfiguration() {
    return shouldHaveNameMatching(DEFAULT_CONFIGURATION_NAME_MATCHING);
  }

  public ArchRule shouldHaveNameMatching(String regex) {
    return classes().that()
        .areAnnotatedWith("org.springframework.context.annotation.Configuration")
        .should().haveNameMatching(regex)
        .as("Configurations should have name ending " + regex);
  }
}
