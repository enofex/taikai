package com.enofex.taikai.logging;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;

final class LoggerConventions {

  private LoggerConventions() {
  }

  static ArchCondition<JavaClass> followLoggerConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>(
        "have a logger field of type %s with name pattern %s and modifiers %s".formatted(
            typeName, regex, requiredModifiers)) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getAllFields()) {
          if (field.getRawType().isAssignableTo(typeName)) {
            if (!field.getName().matches(regex)) {
              events.add(SimpleConditionEvent.violated(field,
                  "Field '%s' in class %s does not match the naming pattern '%s'".formatted(
                      field.getName(),
                      javaClass.getName(), regex)));
            }

            if (!field.getModifiers().containsAll(requiredModifiers)) {
              events.add(SimpleConditionEvent.violated(field,
                  "Field '%s' in class %s does not have the required modifiers %s".formatted(
                      field.getName(),
                      javaClass.getName(),
                      requiredModifiers)));
            }
          }
        }
      }
    };
  }
}
