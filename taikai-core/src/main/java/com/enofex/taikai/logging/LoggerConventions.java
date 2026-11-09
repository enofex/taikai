package com.enofex.taikai.logging;

import static com.enofex.taikai.internal.ArchConditions.haveFieldModifiers;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.regex.Pattern;

final class LoggerConventions {

  private LoggerConventions() {
  }

  static ArchCondition<JavaClass> followLoggerConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers) {
    Pattern name = Pattern.compile(regex);
    ArchCondition<JavaField> modifiers = haveFieldModifiers(requiredModifiers);

    return new ArchCondition<>(
        "have a logger field of type %s with name pattern %s and modifiers %s".formatted(
            typeName, regex, requiredModifiers)) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          if (field.getRawType().isAssignableTo(typeName)) {
            if (!name.matcher(field.getName()).matches()) {
              events.add(SimpleConditionEvent.violated(field,
                  "Field %s in class %s does not have a name matching %s".formatted(
                      field.getName(),
                      javaClass.getName(), regex)));
            }

            modifiers.check(field, events);
          }
        }
      }
    };
  }
}
