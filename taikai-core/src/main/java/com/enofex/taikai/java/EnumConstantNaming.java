package com.enofex.taikai.java;

import static com.enofex.taikai.java.ConstantNaming.CONSTANT_NAME_PATTERN;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class EnumConstantNaming {

  private EnumConstantNaming() {
  }

  static ArchCondition<JavaField> followEnumConstantNamingConventions() {
    return new ArchCondition<>("follow enum constant naming convention") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (field.getModifiers().contains(JavaModifier.ENUM)
            && !CONSTANT_NAME_PATTERN.matcher(field.getName()).matches()) {
          events.add(SimpleConditionEvent.violated(field,
              "Enum constant %s in enum %s does not follow the naming convention".formatted(
                  field.getName(),
                  field.getOwner().getName())));
        }
      }
    };
  }
}
