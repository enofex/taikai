package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class HashCodeAndEquals {

  private HashCodeAndEquals() {
  }

  static ArchCondition<JavaClass> implementHashCodeAndEquals() {
    return new ArchCondition<>("implement both equals and hashCode") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasEquals = javaClass.getMethods().stream()
            .anyMatch(method -> "equals".equals(method.getName()) &&
                method.getRawParameterTypes().size() == 1 &&
                method.getRawParameterTypes().getFirst().getName().equals(Object.class.getName()));

        boolean hasHashCode = javaClass.getMethods().stream()
            .anyMatch(method -> "hashCode".equals(method.getName()) &&
                method.getRawParameterTypes().isEmpty());

        if (hasEquals && !hasHashCode) {
          String message = String.format("Class %s implements equals() but not hashCode()",
              javaClass.getName());
          events.add(SimpleConditionEvent.violated(javaClass, message));
        } else if (!hasEquals && hasHashCode) {
          String message = String.format("Class %s implements hashCode() but not equals()",
              javaClass.getName());
          events.add(SimpleConditionEvent.violated(javaClass, message));
        }
      }
    };
  }
}
