package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isFieldProtected;
import static com.enofex.taikai.internal.Modifiers.isMethodProtected;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class ProtectedMembers {

  private ProtectedMembers() {
  }

  static ArchCondition<JavaClass> notHaveProtectedMembers() {
    return new ArchCondition<>("not have protected members") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          if (isFieldProtected(field)) {
            events.add(SimpleConditionEvent.violated(field,
                "Field %s in final class %s is protected".formatted(
                    field.getName(),
                    javaClass.getName())));
          }
        }

        for (JavaMethod method : javaClass.getMethods()) {
          if (isMethodProtected(method)) {
            events.add(SimpleConditionEvent.violated(method,
                "Method %s in final class %s is protected".formatted(
                    method.getName(),
                    javaClass.getName())));
          }
        }
      }
    };
  }
}