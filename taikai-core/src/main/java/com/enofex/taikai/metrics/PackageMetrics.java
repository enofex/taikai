package com.enofex.taikai.metrics;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

final class PackageMetrics {

  private PackageMetrics() {
  }

  static ArchCondition<JavaClass> notExceedMaxClasses(int maxClasses) {
    return new PackageCondition("not contain more than %d classes".formatted(maxClasses)) {
      @Override
      void check(Map<String, List<JavaClass>> classesByPackage, ConditionEvents events) {
        classesByPackage.forEach((packageName, classes) -> {
          long topLevelClasses = classes.stream()
              .filter(JavaClass::isTopLevelClass)
              .count();

          if (topLevelClasses > maxClasses) {
            events.add(SimpleConditionEvent.violated(classes.get(0).getPackage(),
                "Package %s contains %d classes, max allowed: %d".formatted(
                    packageName, topLevelClasses, maxClasses)));
          }
        });
      }
    };
  }

  static ArchCondition<JavaClass> notExceedMaxPackageDependencies(int maxDependencies) {
    return new PackageCondition("not depend on more than %d packages".formatted(maxDependencies)) {
      @Override
      void check(Map<String, List<JavaClass>> classesByPackage, ConditionEvents events) {
        Map<String, Set<String>> dependedOnPackages = packageDependencies(classesByPackage)
            .collect(groupingBy(PackageDependency::origin, TreeMap::new,
                mapping(PackageDependency::target, toCollection(TreeSet::new))));

        dependedOnPackages.forEach((packageName, targets) -> {
          if (targets.size() > maxDependencies) {
            events.add(SimpleConditionEvent.violated(
                classesByPackage.get(packageName).get(0).getPackage(),
                "Package %s depends on %d packages (%s), max allowed: %d".formatted(
                    packageName, targets.size(), String.join(", ", targets), maxDependencies)));
          }
        });
      }
    };
  }

  static ArchCondition<JavaClass> notExceedMaxRelativeVisibility(double maxRelativeVisibility) {
    return new PackageCondition("not have a relative visibility of more than %s"
        .formatted(maxRelativeVisibility)) {
      @Override
      void check(Map<String, List<JavaClass>> classesByPackage, ConditionEvents events) {
        classesByPackage.forEach((packageName, classes) -> {
          List<JavaClass> topLevelClasses = classes.stream()
              .filter(JavaClass::isTopLevelClass)
              .toList();

          if (topLevelClasses.isEmpty()) {
            return;
          }

          long publicClasses = topLevelClasses.stream()
              .filter(javaClass -> javaClass.getModifiers().contains(JavaModifier.PUBLIC))
              .count();
          double relativeVisibility = rounded((double) publicClasses / topLevelClasses.size());

          if (relativeVisibility > maxRelativeVisibility) {
            events.add(SimpleConditionEvent.violated(classes.get(0).getPackage(),
                String.format(Locale.ROOT,
                    "Package %s has a relative visibility of %.2f (%d of %d classes are public), "
                        + "max allowed: %s",
                    packageName, relativeVisibility, publicClasses, topLevelClasses.size(),
                    maxRelativeVisibility)));
          }
        });
      }
    };
  }

  /**
   * Returns every dependency of a checked class on a checked class in another package, as the
   * pair of their packages.
   */
  private static Stream<PackageDependency> packageDependencies(
      Map<String, List<JavaClass>> classesByPackage) {
    Map<String, String> packageByClass = new HashMap<>();
    classesByPackage.forEach((packageName, classes) ->
        classes.forEach(javaClass -> packageByClass.put(javaClass.getName(), packageName)));

    return classesByPackage.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
            .flatMap(dependency -> {
              String target = packageByClass.get(
                  dependency.getTargetClass().getBaseComponentType().getName());

              return target == null || target.equals(entry.getKey())
                  ? Stream.empty()
                  : Stream.of(new PackageDependency(entry.getKey(), target));
            }));
  }

  /**
   * A dependency of a class in the package {@code origin} on a class in the package
   * {@code target}.
   */
  private record PackageDependency(String origin, String target) {

  }

  /**
   * Rounds a metric to the two decimal places a violation shows, so that a value is compared
   * exactly as it is reported.
   */
  private static double rounded(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * A condition on the packages of the checked classes. It needs all of them at once, so it
   * evaluates the packages after the classes have been collected.
   */
  private abstract static class PackageCondition extends ArchCondition<JavaClass> {

    private final Map<String, List<JavaClass>> classesByPackage = new TreeMap<>();

    PackageCondition(String description) {
      super(description);
    }

    @Override
    public void init(Collection<JavaClass> allObjectsToTest) {
      this.classesByPackage.clear();

      allObjectsToTest.stream()
          .filter(javaClass -> !javaClass.getModifiers().contains(JavaModifier.SYNTHETIC))
          .sorted(Comparator.comparing(JavaClass::getName))
          .forEach(javaClass -> this.classesByPackage
              .computeIfAbsent(javaClass.getPackageName(), packageName -> new ArrayList<>())
              .add(javaClass));
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
    }

    @Override
    public void finish(ConditionEvents events) {
      check(this.classesByPackage, events);
    }

    abstract void check(Map<String, List<JavaClass>> classesByPackage, ConditionEvents events);
  }
}
