package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isFieldFinal;
import static com.enofex.taikai.internal.Modifiers.isFieldStatic;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class SerialVersionUID {

  private SerialVersionUID() {
  }

  static ArchCondition<JavaField> beStaticFinalLong() {
    return new ArchCondition<>("be static final long") {
      @Override
      public void check(JavaField javaField, ConditionEvents events) {
        if (!isFieldStatic(javaField) || !isFieldFinal(javaField) || !isLong(javaField)) {
          events.add(SimpleConditionEvent.violated(javaField,
              "Field %s in class %s is not static final long".formatted(
                  javaField.getName(),
                  javaField.getOwner().getName())));
        }
      }

      private static boolean isLong(JavaField javaField) {
        return javaField.getRawType().isEquivalentTo(long.class);
      }
    };
  }

  static DescribedPredicate<JavaField> namedSerialVersionUID() {
    return new DescribedPredicate<>("named serialVersionUID") {
      @Override
      public boolean test(JavaField javaField) {
        return "serialVersionUID".equals(javaField.getName());
      }
    };
  }
}
