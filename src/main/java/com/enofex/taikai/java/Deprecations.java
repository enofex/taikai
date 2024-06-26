package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class Deprecations {

  private Deprecations() {
  }

  static ArchCondition<JavaClass> notUseDeprecatedAPIs() {
    return new ArchCondition<JavaClass>("not use deprecated APIs") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        javaClass.getFieldAccessesFromSelf().stream()
            .filter(access -> access.getTarget().isAnnotatedWith(Deprecated.class))
            .forEach(access -> events.add(SimpleConditionEvent.violated(access.getTarget(),
                "Field %s in class %s is deprecated and is being accessed by %s".formatted(
                    access.getTarget().getName(),
                    access.getTarget().getOwner().getName(),
                    javaClass.getName()))));

        javaClass.getMethodCallsFromSelf().stream()
            .filter(method -> !method.getTarget().getName().equals(Object.class.getName()))
            .filter(method -> !method.getTarget().getName().equals(Enum.class.getName()))
            .filter(method -> method.getTarget().isAnnotatedWith(Deprecated.class) ||
                method.getTarget().getRawReturnType().isAnnotatedWith(Deprecated.class) ||
                method.getTarget().getParameterTypes().stream()
                    .anyMatch(Deprecations::isDeprecated))
            .forEach(method -> events.add(SimpleConditionEvent.violated(method,
                "Method %s used in class %s is deprecated".formatted(
                    method.getName(),
                    javaClass.getName()))));

        javaClass.getConstructorCallsFromSelf().stream()
            .filter(constructor -> constructor.getTarget().isAnnotatedWith(Deprecated.class) ||
                constructor.getTarget().getParameterTypes().stream()
                    .anyMatch(Deprecations::isDeprecated))
            .forEach(constructor -> events.add(SimpleConditionEvent.violated(constructor,
                "Constructor %s in class %s uses deprecated APIs".formatted(
                    constructor.getTarget().getFullName(),
                    javaClass.getName()))));

        javaClass.getDirectDependenciesFromSelf().stream()
            .filter(dependency -> dependency.getTargetClass().isAnnotatedWith(Deprecated.class))
            .forEach(dependency -> events.add(
                SimpleConditionEvent.violated(dependency.getTargetClass(),
                    "Class %s depends on deprecated class %s".formatted(
                        javaClass.getName(),
                        dependency.getTargetClass().getName()))));
      }
    }.as("no usage of deprecated APIs");
  }

  private static boolean isDeprecated(JavaType javaType) {
    return javaType.toErasure().isAnnotatedWith(Deprecated.class);
  }
}
