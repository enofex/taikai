package com.enofex.taikai;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class JavaPredicates {

  private JavaPredicates() {
  }

  public static DescribedPredicate<CanBeAnnotated> annotatedWith(String annotation,
      boolean isMetaAnnotated) {
    return new DescribedPredicate<>("annotated with %s".formatted(annotation)) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return isMetaAnnotated ? canBeAnnotated.isMetaAnnotatedWith(annotation) :
            canBeAnnotated.isAnnotatedWith(annotation);
      }
    };
  }

  public static ArchCondition<JavaMethod> notDeclareThrownExceptions() {
    return new ArchCondition<>("not declare thrown exceptions") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (!method.getThrowsClause().isEmpty()) {
          String message = String.format("Method %s declares thrown exceptions",
              method.getFullName());
          events.add(SimpleConditionEvent.violated(method, message));
        }
      }
    };
  }

  public static ArchCondition<JavaField> notBePublic() {
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

  public static DescribedPredicate<JavaClass> areFinal() {
    return new DescribedPredicate<>("are final") {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.getModifiers().contains(JavaModifier.FINAL);
      }
    };
  }


  public static ArchCondition<JavaClass> beFinal() {
    return new ArchCondition<>("be final") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean isFinal = javaClass.getModifiers().contains(JavaModifier.FINAL);
        String message = String.format("Class %s is not final", javaClass.getName());
        events.add(new SimpleConditionEvent(javaClass, isFinal, message));
      }
    };
  }
}
