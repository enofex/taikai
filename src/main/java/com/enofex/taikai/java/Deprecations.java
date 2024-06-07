package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
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
        if (item.isAnnotatedWith(Deprecated.class)) {
          events.add(SimpleConditionEvent.violated(item,
              String.format("Class %s is deprecated", item.getName())));
        }

        item.getAllFields().stream()
            .filter(field -> field.isAnnotatedWith(Deprecated.class))
            .forEach(field -> events.add(SimpleConditionEvent.violated(field,
                String.format("Field %s in class %s is deprecated", field.getName(),
                    item.getName()))));

        item.getAllMethods().stream()
            .filter(method -> !method.getOwner().getName().equals(Object.class.getName()))
            .filter(method -> !method.getOwner().getName().equals(Enum.class.getName()))
            .filter(method -> method.isAnnotatedWith(Deprecated.class))
            .forEach(method -> events.add(SimpleConditionEvent.violated(method,
                String.format("Method %s in class %s is deprecated", method.getName(),
                    item.getName()))));

        item.getAllConstructors().stream()
            .filter(constructor -> constructor.isAnnotatedWith(Deprecated.class))
            .forEach(constructor -> events.add(SimpleConditionEvent.violated(constructor,
                String.format("Constructor %s in class %s is deprecated",
                    constructor.getFullName(), item.getName()))));

        item.getDirectDependenciesFromSelf().stream()
            .filter(dependency -> dependency.getTargetClass().isAnnotatedWith(Deprecated.class))
            .forEach(dependency -> events.add(
                SimpleConditionEvent.violated(dependency.getTargetClass(),
                    String.format("Class %s references deprecated class %s", item.getName(),
                        dependency.getTargetClass().getName()))));
      }
    }.as("No usage of deprecated APIs");
  }
}
