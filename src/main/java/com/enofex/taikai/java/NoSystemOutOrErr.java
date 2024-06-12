package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class NoSystemOutOrErr {

  private NoSystemOutOrErr() {
  }

  static ArchCondition<JavaClass> notUseSystemOutOrErr() {
    return new ArchCondition<>("not call System.out or System.err") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        javaClass.getFieldAccessesFromSelf().stream()
            .filter(fieldAccess -> fieldAccess.getTargetOwner().isEquivalentTo(System.class))
            .forEach(fieldAccess -> {
              String fieldName = fieldAccess.getTarget().getName();

              if ("out".equals(fieldName) || "err".equals(fieldName)) {
                String message = String.format("Method %s calls %s.%s",
                    fieldAccess.getOrigin().getFullName(),
                    fieldAccess.getTargetOwner().getName(),
                    fieldAccess.getTarget().getName());

                events.add(SimpleConditionEvent.violated(fieldAccess, message));
              }
            });
      }
    };
  }
}
