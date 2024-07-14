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

  public ImportsConfigurer shouldNotImport(String packageIdentifier) {
    return shouldNotImport(packageIdentifier, Configuration.defaultConfiguration());
  }

  public ImportsConfigurer shouldNotImport(String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().accessClassesThat()
        .resideInAPackage(packageIdentifier)
            .as("No classes should have imports from package %s".formatted(packageIdentifier)),
        configuration));
  }

  public ImportsConfigurer shouldNotImport(String regex, String notImportClassesRegex) {
    return shouldNotImport(regex, notImportClassesRegex, Configuration.defaultConfiguration());
  }

  public ImportsConfigurer shouldNotImport(String regex, String notImportClassesRegex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that().haveNameMatching(regex)
        .should().accessClassesThat()
        .haveNameMatching(notImportClassesRegex)
        .as("No classes that have name matching %s should have imports %s".formatted(
            regex, notImportClassesRegex)), configuration));
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
