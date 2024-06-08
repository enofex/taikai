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
      public void check(JavaClass item, ConditionEvents events) {
        item.getFieldAccessesFromSelf().stream()
            .filter(access -> access.getTarget().isAnnotatedWith(Deprecated.class))
            .forEach(access -> events.add(SimpleConditionEvent.violated(access.getTarget(),
                String.format("Field %s in class %s is deprecated and is being accessed by %s",
                    access.getTarget().getName(), access.getTarget().getOwner().getName(),
                    item.getName()))));

        item.getMethodCallsFromSelf().stream()
            .filter(method -> !method.getOwner().getName().equals(Object.class.getName()))
            .filter(method -> !method.getOwner().getName().equals(Enum.class.getName()))
            .filter(method -> method.getOwner().isAnnotatedWith(Deprecated.class) ||
                method.getOwner().getRawReturnType().isAnnotatedWith(Deprecated.class) ||
                method.getOwner().getParameterTypes().stream().anyMatch(Deprecations::isDeprecated)
                || method.getOwner().getCallsFromSelf().stream()
                .anyMatch(call -> call.getTarget().isAnnotatedWith(Deprecated.class)))
            .forEach(method -> events.add(SimpleConditionEvent.violated(method,
                String.format("Method %s in class %s uses deprecated APIs", method.getName(),
                    item.getName()))));

        item.getConstructorCallsFromSelf().stream()
            .filter(constructor -> constructor.getTarget().isAnnotatedWith(Deprecated.class) ||
                constructor.getTarget().getParameterTypes().stream()
                    .anyMatch(Deprecations::isDeprecated))
            .forEach(constructor -> events.add(SimpleConditionEvent.violated(constructor,
                String.format("Constructor %s in class %s uses deprecated APIs",
                    constructor.getTarget().getFullName(), item.getName()))));

        item.getDirectDependenciesFromSelf().stream()
            .filter(dependency -> dependency.getTargetClass().isAnnotatedWith(Deprecated.class))
            .forEach(dependency -> events.add(
                SimpleConditionEvent.violated(dependency.getTargetClass(),
                    String.format("Class %s depends on deprecated class %s", item.getName(),
                        dependency.getTargetClass().getName()))));
      }
    }.as("no usage of deprecated APIs");
  }

  private static boolean isDeprecated(JavaType javaType) {
    return javaType.toErasure().isAnnotatedWith(Deprecated.class);
  }
}
