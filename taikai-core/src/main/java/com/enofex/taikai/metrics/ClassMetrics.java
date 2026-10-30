package com.enofex.taikai.metrics;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class ClassMetrics {

  private ClassMetrics() {
  }

  /**
   * Selects the classes a rule checks. Each carries the names of all checked classes, so that the
   * rule counts only those, including the ones outside the selection.
   */
  static ClassesTransformer<CheckedClass> checkedClasses(
      DescribedPredicate<? super JavaClass> classes) {
    return new AbstractClassesTransformer<>("classes " + classes.getDescription()) {
      @Override
      public Iterable<CheckedClass> doTransform(JavaClasses javaClasses) {
        Set<String> checkedClasses = javaClasses.stream()
            .map(JavaClass::getName)
            .collect(toUnmodifiableSet());

        return javaClasses.stream()
            .filter(classes)
            .map(javaClass -> new CheckedClass(javaClass, List.of(), checkedClasses))
            .toList();
      }
    };
  }

  /**
   * Like {@link #checkedClasses(DescribedPredicate)}, but selects top-level classes, each with the
   * classes nested in it at any depth.
   */
  static ClassesTransformer<CheckedClass> checkedTopLevelClasses(
      DescribedPredicate<? super JavaClass> classes) {
    return new AbstractClassesTransformer<>("top-level classes " + classes.getDescription()) {
      @Override
      public Iterable<CheckedClass> doTransform(JavaClasses javaClasses) {
        Set<String> checkedClasses = javaClasses.stream()
            .map(JavaClass::getName)
            .collect(toUnmodifiableSet());
        Map<String, List<JavaClass>> classesByTopLevelClass = javaClasses.stream()
            .filter(javaClass -> !javaClass.isTopLevelClass())
            .collect(groupingBy(javaClass -> topLevelClass(javaClass).getName()));

        return javaClasses.stream()
            .filter(JavaClass::isTopLevelClass)
            .filter(classes)
            .map(javaClass -> new CheckedClass(javaClass,
                classesByTopLevelClass.getOrDefault(javaClass.getName(), List.of()),
                checkedClasses))
            .toList();
      }
    };
  }

  static ArchCondition<CheckedClass> notExceedMaxInheritanceDepth(int maxInheritanceDepth) {
    return new ArchCondition<>(
        "not have an inheritance depth of more than %d".formatted(maxInheritanceDepth)) {
      @Override
      public void check(CheckedClass checkedClass, ConditionEvents events) {
        JavaClass javaClass = checkedClass.javaClass();
        long inheritanceDepth = javaClass.getAllRawSuperclasses().stream()
            .filter(checkedClass::isChecked)
            .count();

        if (inheritanceDepth > maxInheritanceDepth) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s has an inheritance depth of %d, max allowed: %d".formatted(
                  javaClass.getName(), inheritanceDepth, maxInheritanceDepth)));
        }
      }
    };
  }

  static ArchCondition<CheckedClass> notExceedMaxSubclasses(int maxSubclasses) {
    return new ArchCondition<>("not have more than %d subclasses".formatted(maxSubclasses)) {
      @Override
      public void check(CheckedClass checkedClass, ConditionEvents events) {
        JavaClass javaClass = checkedClass.javaClass();
        long subclasses = javaClass.getSubclasses().stream()
            .filter(subclass -> !subclass.isAnonymousClass())
            .filter(checkedClass::isChecked)
            .count();

        if (subclasses > maxSubclasses) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s has %d subclasses, max allowed: %d".formatted(
                  javaClass.getName(), subclasses, maxSubclasses)));
        }
      }
    };
  }

  static ArchCondition<JavaMethod> notExceedMaxMethodParameters(int maxMethodParameters) {
    return new ArchCondition<>("not have more than %d parameters".formatted(maxMethodParameters)) {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        int parameterCount = method.getRawParameterTypes().size();

        if (parameterCount > maxMethodParameters) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s has %d parameters, max allowed: %d".formatted(
                  method.getFullName(), parameterCount, maxMethodParameters)));
        }
      }
    };
  }

  static ArchCondition<CheckedClass> notExceedMaxDependencies(int maxDependencies) {
    return new ArchCondition<>("not depend on more than %d classes".formatted(maxDependencies)) {
      @Override
      public void check(CheckedClass checkedClass, ConditionEvents events) {
        JavaClass javaClass = checkedClass.javaClass();
        long dependencies = Stream.concat(Stream.of(javaClass),
                checkedClass.nestedClasses().stream())
            .flatMap(declaringClass -> declaringClass.getDirectDependenciesFromSelf().stream())
            .map(dependency -> dependency.getTargetClass().getBaseComponentType())
            .filter(checkedClass::isChecked)
            .map(target -> topLevelClass(target).getName())
            .filter(target -> !target.equals(javaClass.getName()))
            .distinct()
            .count();

        if (dependencies > maxDependencies) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s depends on %d classes, max allowed: %d".formatted(
                  javaClass.getName(), dependencies, maxDependencies)));
        }
      }
    };
  }

  private static JavaClass topLevelClass(JavaClass javaClass) {
    JavaClass topLevelClass = javaClass;

    while (topLevelClass.getEnclosingClass().isPresent()) {
      topLevelClass = topLevelClass.getEnclosingClass().get();
    }

    return topLevelClass;
  }

  /**
   * A class a rule checks, together with the names of all classes the rule checks and, for a
   * top-level class, the classes nested in it.
   */
  static final class CheckedClass {

    private final JavaClass javaClass;
    private final List<JavaClass> nestedClasses;
    private final Set<String> checkedClasses;

    private CheckedClass(JavaClass javaClass, List<JavaClass> nestedClasses,
        Set<String> checkedClasses) {
      this.javaClass = javaClass;
      this.nestedClasses = nestedClasses;
      this.checkedClasses = checkedClasses;
    }

    JavaClass javaClass() {
      return this.javaClass;
    }

    List<JavaClass> nestedClasses() {
      return this.nestedClasses;
    }

    boolean isChecked(JavaClass javaClass) {
      return this.checkedClasses.contains(javaClass.getName());
    }

    @Override
    public String toString() {
      return this.javaClass.getName();
    }
  }
}
