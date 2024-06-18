package com.enofex.taikai.java;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;

final class UtilityClasses {

  private UtilityClasses() {
  }

  static GivenClassesConjunction utilityClasses() {
    return classes().that(haveOnlyStaticMethods());
  }

  private static DescribedPredicate<JavaClass> haveOnlyStaticMethods() {
    return new DescribedPredicate<>("have only static methods") {
      @Override
      public boolean test(JavaClass javaClass) {
        return !javaClass.getMethods().isEmpty() && javaClass.getMethods().stream()
            .allMatch(method -> method.getModifiers().contains(JavaModifier.STATIC)
                && !"main".equals(method.getName()));
      }
    };
  }

  static ArchCondition<JavaClass> havePrivateConstructor() {
    return new ArchCondition<>("have a private constructor") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean hasPrivateConstructor = javaClass.getConstructors().stream()
            .anyMatch(constructor -> constructor.getModifiers().contains(JavaModifier.PRIVATE));
        String message = String.format("Class %s does not have a private constructor",
            javaClass.getName());
        events.add(new SimpleConditionEvent(javaClass, hasPrivateConstructor, message));
      }
    };
  }
}
