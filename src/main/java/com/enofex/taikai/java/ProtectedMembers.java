package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
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
          if (field.getModifiers().contains(JavaModifier.PROTECTED)) {
            String message = String.format("Field %s in final class %s is protected",
                field.getName(), javaClass.getName());
            events.add(SimpleConditionEvent.violated(field, message));
          }
        }
        for (JavaMethod method : javaClass.getMethods()) {
          if (method.getModifiers().contains(JavaModifier.PROTECTED)) {
            String message = String.format("Method %s in final class %s is protected",
                method.getName(), javaClass.getName());
            events.add(SimpleConditionEvent.violated(method, message));
          }
        }
      }
    };
  }
}
