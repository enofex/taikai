package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isFieldSynthetic;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.regex.Pattern;

final class ConstantNaming {

  private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

  private ConstantNaming() {
  }

  static ArchCondition<JavaField> shouldFollowConstantNamingConventions(
      Collection<String> excludedFields) {
    return new ArchCondition<>("follow constant naming convention") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!isFieldSynthetic(field)
            && !excludedFields.contains(field.getName())
            && !CONSTANT_NAME_PATTERN.matcher(field.getName()).matches()) {
          events.add(SimpleConditionEvent.violated(field,
              "Constant %s in class %s does not follow the naming convention".formatted(
                  field.getName(),
                  field.getOwner().getName())));
        }
      }
    };
  }
}
