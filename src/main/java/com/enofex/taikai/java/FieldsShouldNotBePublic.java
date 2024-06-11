package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class FieldsShouldNotBePublic {

  private FieldsShouldNotBePublic() {
  }

  static ArchCondition<JavaField> notBePublic() {
    return new ArchCondition<>("not be public") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!field.getModifiers().contains(JavaModifier.STATIC)
            && field.getModifiers().contains(JavaModifier.PUBLIC)) {
          String message = String.format("Field %s in class %s is public",
              field.getName(),
              field.getOwner().getFullName());
          events.add(SimpleConditionEvent.violated(field, message));
        }
      }
    };
  }
}
