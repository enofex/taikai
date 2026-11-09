package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class InterfaceNaming {

  private InterfaceNaming() {
  }

  /**
   * Creates an {@link ArchCondition} that fails if an interface name starts with {@code I} followed
   * by an uppercase character, for example {@code ICustomer}.
   *
   * @return the condition that detects {@code I}-prefixed interfaces
   */
  static ArchCondition<JavaClass> notBePrefixedWithI() {
    return new ArchCondition<>("not be prefixed with I") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        String simpleName = javaClass.getSimpleName();

        if (simpleName.length() > 1 && simpleName.charAt(0) == 'I'
            && Character.isUpperCase(simpleName.charAt(1))) {
          events.add(SimpleConditionEvent.violated(javaClass, simpleName));
        }
      }
    };
  }
}
