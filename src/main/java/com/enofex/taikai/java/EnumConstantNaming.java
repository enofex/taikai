package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.regex.Pattern;

final class EnumConstantNaming {

  private static final Pattern ENUM_CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

  private EnumConstantNaming() {
  }

  static ArchCondition<JavaField> shouldFollowEnumConstantNamingConventions() {
    return new ArchCondition<>("follow enum constant naming convention") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (field.getModifiers().contains(JavaModifier.ENUM)
            && !ENUM_CONSTANT_NAME_PATTERN.matcher(field.getName()).matches()) {
          events.add(SimpleConditionEvent.violated(field,
              "Enum constant %s in enum %s does not follow the naming convention".formatted(
                  field.getName(),
                  field.getOwner().getName())));
        }
      }
    };
  }
}
