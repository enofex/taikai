package com.enofex.taikai.java;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
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
        boolean isStatic = javaField.getModifiers().contains(JavaModifier.STATIC);
        boolean isFinal = javaField.getModifiers().contains(JavaModifier.FINAL);
        boolean isLong = javaField.getRawType().isEquivalentTo(long.class);

        if (!isStatic || !isFinal || !isLong) {
          String message = String.format("Field %s in class %s is not static final long",
              javaField.getName(), javaField.getOwner().getName());
          events.add(SimpleConditionEvent.violated(javaField, message));
        }
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
