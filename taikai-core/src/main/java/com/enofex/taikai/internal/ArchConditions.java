package com.enofex.taikai.internal;

import static com.enofex.taikai.internal.Modifiers.isPublic;
import static com.enofex.taikai.internal.Modifiers.isStatic;
import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.Comparator;

/**
 * {@link ArchCondition}s that several rules share.
 */
public final class ArchConditions {

  private ArchConditions() {
  }

  /**
   * Returns a condition that a method has no matching type in its declared return signature,
   * including generic arguments, bounds and array component types.
   *
   * @param forbiddenTypes the predicate selecting forbidden return types
   * @return the condition
   */
  public static ArchCondition<JavaMethod> notReturnTypesMatching(
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    requireNonNull(forbiddenTypes);
    return new ArchCondition<>("not return types " + forbiddenTypes.getDescription()) {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        method.getReturnType().getAllInvolvedRawTypes().stream()
            .filter(forbiddenTypes)
            .sorted(Comparator.comparing(JavaClass::getName))
            .forEach(type -> events.add(SimpleConditionEvent.violated(method,
                "Method %s has forbidden return type %s in signature %s %s".formatted(
                    method.getFullName(), type.getName(), method.getReturnType().getName(),
                    method.getSourceCodeLocation()))));
      }
    };
  }

  /**
   * Returns a condition that a method has at least one matching type in its declared return
   * signature, including generic arguments, bounds and array component types.
   *
   * @param requiredTypes the predicate selecting required return types
   * @return the condition
   */
  public static ArchCondition<JavaMethod> returnTypesMatching(
      DescribedPredicate<? super JavaClass> requiredTypes) {
    requireNonNull(requiredTypes);
    return new ArchCondition<>("return types " + requiredTypes.getDescription()) {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (method.getReturnType().getAllInvolvedRawTypes().stream().noneMatch(requiredTypes)) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s has no return type %s in signature %s %s".formatted(
                  method.getFullName(), requiredTypes.getDescription(),
                  method.getReturnType().getName(), method.getSourceCodeLocation())));
        }
      }
    };
  }

  /**
   * Returns a condition that a field has no matching type in its declared type, including generic
   * arguments, bounds and array component types.
   *
   * @param forbiddenTypes the predicate selecting forbidden field types
   * @return the condition
   */
  public static ArchCondition<JavaField> notHaveFieldTypesMatching(
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    requireNonNull(forbiddenTypes);
    return new ArchCondition<>("not be of types " + forbiddenTypes.getDescription()) {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        field.getType().getAllInvolvedRawTypes().stream()
            .filter(forbiddenTypes)
            .sorted(Comparator.comparing(JavaClass::getName))
            .forEach(type -> events.add(SimpleConditionEvent.violated(field,
                "Field %s has forbidden type %s in signature %s %s".formatted(
                    field.getFullName(), type.getName(), field.getType().getName(),
                    field.getSourceCodeLocation()))));
      }
    };
  }

  /**
   * Returns a condition that a method declares no thrown exceptions.
   *
   * @return the condition
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
   * Returns a condition that a field is not public, unless it is static.
   *
   * @return the condition
   */
  public static ArchCondition<JavaField> notBePublicUnlessStatic() {
    return new ArchCondition<>("not be public") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!isStatic(field) && isPublic(field)) {
          events.add(SimpleConditionEvent.violated(field,
              "Field %s in class %s is public".formatted(
                  field.getName(),
                  field.getOwner().getFullName())));
        }
      }
    };
  }

  /**
   * Returns a condition that a class has at least one field, declared or inherited, of exactly the
   * given type.
   *
   * @param typeName the fully qualified name of the type
   * @return the condition
   */
  public static ArchCondition<JavaClass> haveFieldOfType(String typeName) {
    return new ArchCondition<>("have a field of type %s".formatted(typeName)) {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean hasFieldOfType = item.getAllFields().stream()
            .anyMatch(field -> field.getRawType().getName().equals(typeName));

        if (!hasFieldOfType) {
          events.add(SimpleConditionEvent.violated(item,
              "%s does not have a field of type %s".formatted(
                  item.getName(),
                  typeName)));
        }
      }
    };
  }

  /**
   * Returns a condition that a class has at least one field, declared or inherited, matching the
   * given predicate. Synthetic fields are ignored.
   *
   * @param requiredFields the predicate selecting the required field
   * @return the condition
   */
  public static ArchCondition<JavaClass> haveFieldMatching(
      DescribedPredicate<? super JavaField> requiredFields) {
    requireNonNull(requiredFields);
    return new ArchCondition<>("have a field " + requiredFields.getDescription()) {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean hasField = item.getAllFields().stream()
            .filter(field -> !Modifiers.isSynthetic(field))
            .anyMatch(requiredFields);

        if (!hasField) {
          events.add(SimpleConditionEvent.violated(item,
              "%s does not have a field %s %s".formatted(
                  item.getName(), requiredFields.getDescription(),
                  item.getSourceCodeLocation())));
        }
      }
    };
  }

  /**
   * Returns a condition that a field has all the given modifiers.
   *
   * @param requiredModifiers the modifiers that the field must have
   * @return the condition
   */
  public static ArchCondition<JavaField> haveFieldModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("have field modifiers") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!field.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(field,
              "Field %s in class %s is missing one of these modifiers: %s".formatted(
                  field.getName(),
                  field.getOwner().getFullName(),
                  Modifiers.names(requiredModifiers))));
        }
      }
    };
  }

  /**
   * Returns a condition that a field has none of the given modifiers.
   *
   * @param forbiddenModifiers the modifiers that the field must not have
   * @return the condition
   */
  public static ArchCondition<JavaField> notHaveFieldModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("not have field modifiers") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        String presentModifiers = presentForbiddenModifiers(field.getModifiers(),
            forbiddenModifiers);

        if (!presentModifiers.isEmpty()) {
          events.add(SimpleConditionEvent.violated(field,
              "Field %s in class %s has forbidden modifier(s): %s".formatted(
                  field.getName(),
                  field.getOwner().getFullName(),
                  presentModifiers)));
        }
      }
    };
  }

  /**
   * Returns a condition that a method has all the given modifiers.
   *
   * @param requiredModifiers the modifiers that the method must have
   * @return the condition
   */
  public static ArchCondition<JavaMethod> haveMethodModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("have method modifiers") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (!method.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s in class %s is missing one of these modifiers: %s".formatted(
                  method.getName(),
                  method.getOwner().getFullName(),
                  Modifiers.names(requiredModifiers))));
        }
      }
    };
  }

  /**
   * Returns a condition that a method has none of the given modifiers.
   *
   * @param forbiddenModifiers the modifiers that the method must not have
   * @return the condition
   */
  public static ArchCondition<JavaMethod> notHaveMethodModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("not have method modifiers") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        String presentModifiers = presentForbiddenModifiers(method.getModifiers(),
            forbiddenModifiers);

        if (!presentModifiers.isEmpty()) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s in class %s has forbidden modifier(s): %s".formatted(
                  method.getName(),
                  method.getOwner().getFullName(),
                  presentModifiers)));
        }
      }
    };
  }

  /**
   * Returns a condition that a class has all the given modifiers.
   *
   * @param requiredModifiers the modifiers that the class must have
   * @return the condition
   */
  public static ArchCondition<JavaClass> haveClassModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("have class modifiers") {
      @Override
      public void check(JavaClass clazz, ConditionEvents events) {
        if (!clazz.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(clazz,
              "Class %s is missing one of these modifiers: %s".formatted(
                  clazz.getName(),
                  Modifiers.names(requiredModifiers))));
        }
      }
    };
  }

  /**
   * Returns a condition that a class has none of the given modifiers.
   *
   * @param forbiddenModifiers the modifiers that the class must not have
   * @return the condition
   */
  public static ArchCondition<JavaClass> notHaveClassModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("not have class modifiers") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        String presentModifiers = presentForbiddenModifiers(javaClass.getModifiers(),
            forbiddenModifiers);

        if (!presentModifiers.isEmpty()) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s has forbidden modifier(s): %s".formatted(
                  javaClass.getFullName(),
                  presentModifiers)));
        }
      }
    };
  }

  private static String presentForbiddenModifiers(Collection<JavaModifier> modifiers,
      Collection<JavaModifier> forbiddenModifiers) {
    return Modifiers.names(modifiers.stream()
        .filter(forbiddenModifiers::contains)
        .sorted()
        .toList());
  }
}
