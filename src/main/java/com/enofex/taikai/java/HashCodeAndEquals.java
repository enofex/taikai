package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class HashCodeAndEquals {

  private HashCodeAndEquals() {
  }

  static ArchCondition<JavaClass> implementHashCodeAndEquals() {
    return new ArchCondition<>("implement both equals() and hashCode()") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasEquals = hasEquals(javaClass);
        boolean hasHashCode = hasHashCode(javaClass);

        if (hasEquals && !hasHashCode) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s implements equals() but not hashCode()".formatted(
                  javaClass.getName())));
        } else if (!hasEquals && hasHashCode) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s implements hashCode() but not equals()".formatted(
                  javaClass.getName())));
        }
      }

      private static boolean hasHashCode(JavaClass javaClass) {
        return javaClass.getMethods().stream()
            .anyMatch(method -> "hashCode".equals(method.getName()) &&
                method.getRawParameterTypes().isEmpty());
      }

      private static boolean hasEquals(JavaClass javaClass) {
        return javaClass.getMethods().stream()
            .anyMatch(method -> "equals".equals(method.getName()) &&
                method.getRawParameterTypes().size() == 1 &&
                method.getRawParameterTypes().get(0).getName().equals(Object.class.getName()));
      }
    };
  }
}
