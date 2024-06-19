package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.regex.Pattern;

final class ConstantNaming {

  private ConstantNaming() {
  }

  static ArchCondition<JavaField> shouldFollowConstantNamingConvention() {
    return new ArchCondition<>("follow constant naming convention") {

      private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!field.getOwner().isEnum()
            && !"serialVersionUID".equals(field.getName())
            && !CONSTANT_NAME_PATTERN.matcher(field.getName()).matches()) {
          String message = String.format(
              "Constant %s in class %s does not follow the naming convention", field.getName(),
              field.getOwner().getName());
          events.add(SimpleConditionEvent.violated(field, message));
        }
      }
    };
  }
}
