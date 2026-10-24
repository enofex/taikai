package com.enofex.taikai;

import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds configuration mistakes that do not fail a rule, but let it check less than intended: a
 * namespace without classes to check, such as a misspelled one, and exclusions that match no class.
 */
final class ConfigurationWarnings {

  private ConfigurationWarnings() {
  }

  static List<String> of(Taikai taikai) {
    Set<Import> emptyImports = new LinkedHashSet<>();
    Set<Import> imports = new LinkedHashSet<>();
    List<String> ruleWarnings = new ArrayList<>();

    for (TaikaiRule rule : taikai.rules()) {
      Configuration configuration = rule.configuration();
      JavaClasses configuredClasses = configuration.javaClasses();

      if (configuredClasses != null) {
        ruleWarnings.addAll(unusedExclusions(rule, configuredClasses));
      } else if (taikai.classes() == null) {
        String namespace = configuration.namespace() != null
            ? configuration.namespace()
            : taikai.namespace();

        if (namespace != null) {
          Import namespaceImport = new Import(namespace, configuration.namespaceImport());

          if (namespaceImport.classes().isEmpty()) {
            emptyImports.add(namespaceImport);
          } else {
            imports.add(namespaceImport);
            ruleWarnings.addAll(unusedExclusions(rule, namespaceImport.classes()));
          }
        }
      }
    }

    List<String> warnings = new ArrayList<>();

    emptyImports.forEach(emptyImport -> warnings.add(("Namespace %s contains no %s, so the rules "
        + "for them check nothing. Is the namespace correct?")
        .formatted(emptyImport.namespace(), emptyImport.contents())));

    if (!imports.isEmpty()) {
      taikai.excludedClasses().stream()
          .filter(excludedClass -> imports.stream()
              .noneMatch(used -> TaikaiRule.excludesAny(excludedClass, used.classes())))
          .forEach(excludedClass -> warnings.add(unusedExclusion(excludedClass)));

      taikai.excludedCriteria().stream()
          .filter(criteria -> imports.stream()
              .noneMatch(used -> excludesAny(criteria, used.classes())))
          .forEach(criteria -> warnings.add(unusedExclusion(criteria.getDescription())));
    }

    warnings.addAll(ruleWarnings);

    return warnings;
  }

  private static List<String> unusedExclusions(TaikaiRule rule, JavaClasses classes) {
    return rule.configuration().excludedClasses().stream()
        .filter(excludedClass -> !TaikaiRule.excludesAny(excludedClass, classes))
        .map(excludedClass -> "Exclusion '%s' of rule '%s' matches no class, so it can be removed"
            .formatted(excludedClass, rule.archRule().getDescription()))
        .toList();
  }

  private static boolean excludesAny(DescribedPredicate<? super JavaClass> criteria,
      JavaClasses classes) {
    return classes.stream().anyMatch(criteria);
  }

  private static String unusedExclusion(String exclusion) {
    return "Exclusion '%s' matches no class, so it can be removed".formatted(exclusion);
  }

  private record Import(String namespace, Namespace.IMPORT mode) {

    JavaClasses classes() {
      return Namespace.from(this.namespace, this.mode);
    }

    String contents() {
      return switch (this.mode) {
        case WITHOUT_TESTS -> "production classes";
        case ONLY_TESTS -> "test classes";
        case WITH_TESTS -> "classes";
      };
    }
  }
}
