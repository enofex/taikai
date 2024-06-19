package com.enofex.taikai.internal;

import static com.enofex.taikai.internal.Modifiers.isFieldPublic;
import static com.enofex.taikai.internal.Modifiers.isFieldStatic;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Internal utility class for defining general ArchCondition used in architectural rules.
 * <p>
 * This class is intended for internal use only and is not part of the public API. Developers should
 * not rely on this class for any public API usage.
 */
public final class ArchConditions {

  private ArchConditions() {
  }

  /**
   * Creates a condition that checks if a method does not declare thrown exceptions.
   *
   * @return an architectural condition for checking thrown exceptions in methods
   */
  public static ArchCondition<JavaMethod> notDeclareThrownExceptions() {
    return new ArchCondition<>("not declare thrown exceptions") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (!method.getThrowsClause().isEmpty()) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s declares thrown exceptions".formatted(
                  method.getFullName())));
        }
      }
    };
  }

  /**
   * Creates a condition that checks if a field is not public and not static.
   *
   * @return an architectural condition for checking public except static fields
   */
  public static ArchCondition<JavaField> notBePublicButNotStatic() {
    return new ArchCondition<>("not be public") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!isFieldStatic(field) && isFieldPublic(field)) {
          events.add(SimpleConditionEvent.violated(field,
              "Field %s in class %s is public".formatted(
                  field.getName(),
                  field.getOwner().getFullName())));
        }
      }
    };
  }
}
