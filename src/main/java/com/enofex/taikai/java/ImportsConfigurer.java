package com.enofex.taikai.java;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.enofex.taikai.TaikaiException;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class ImportsConfigurer extends AbstractConfigurer {

  ImportsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public ImportsConfigurer shouldNotImport(String regex) {
    return shouldNotImport(regex, null);
  }

  public ImportsConfigurer shouldNotImport(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().accessClassesThat()
        .resideInAPackage(regex)
        .as("No classes should have imports from %s".formatted(regex)), configuration));
  }

  public ImportsConfigurer shouldHaveNoCycles() {
    return shouldHaveNoCycles(null);
  }

  public ImportsConfigurer shouldHaveNoCycles(Configuration configuration) {
    String namespace = configuration != null
        ? configuration.namespace()
        : configurerContext() != null
            ? configurerContext().namespace()
            : null;

    if (namespace == null) {
      throw new TaikaiException("Namespace is not set");
    }

    return addRule(TaikaiRule.of(slices()
        .matching(namespace + ".(*)..")
        .should().beFreeOfCycles()
        .as("Namespace %s should be free of cycles".formatted(namespace)), configuration));
  }
}
