package com.enofex.taikai.internal;

import static com.enofex.taikai.internal.Modifiers.isFieldPublic;
import static com.enofex.taikai.internal.Modifiers.isFieldStatic;
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
import java.util.stream.Collectors;

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
   * Rejects matching types anywhere in a method's declared return signature, including
   * generic arguments, bounds, and array component types.
   *
   * @param forbiddenTypes the predicate selecting forbidden return types
   * @return the return type condition
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
   * Requires at least one matching type in each method's declared return signature, including
   * generic arguments, bounds, and array component types.
   *
   * @param requiredTypes the predicate selecting required return types
   * @return the return type condition
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
   * Prevents forbidden types in a field's declared type, including generic arguments, bounds, and
   * array component types.
   *
   * @param forbiddenTypes the predicate selecting forbidden field types
   * @return the field type condition
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
  public static ArchCondition<JavaField> notBePublicUnlessStatic() {
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

  /**
   * Creates a condition that checks if a class has a field of the specified type.
   *
   * @param typeName the name of the type to check for in the fields of the class
   * @return an architectural condition for checking if a class has a field of the specified type
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
   * Creates a condition that checks if a field contains all the specified modifiers.
   *
   * @param requiredModifiers the collection of modifiers that the field is required to have
   * @return an architectural condition for checking if a field has the required modifiers
   */
  public static ArchCondition<JavaField> hasFieldModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("has field modifiers") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!field.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(field,
              "Field %s in class %s is missing one of these modifiers: %s".formatted(
                  field.getName(),
                  field.getOwner().getFullName(),
                  requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))));
        }
      }
    };
  }

  /**
   * Creates a condition that checks if a field does <strong>not</strong> contain
   * any of the specified modifiers.
   *
   * @param forbiddenModifiers the collection of modifiers that the field must not have
   * @return an architectural condition for checking that a field does not have the given modifiers
   */
  public static ArchCondition<JavaField> notHasFieldModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("does not have field modifiers") {
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
   * Creates a condition that checks if a method contains all the specified modifiers.
   *
   * @param requiredModifiers the collection of modifiers that the method is required to have
   * @return an architectural condition for checking if a method has the required modifiers
   */
  public static ArchCondition<JavaMethod> hasMethodsModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("has method modifiers") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (!method.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s in class %s is missing one of these modifiers: %s".formatted(
                  method.getName(),
                  method.getOwner().getFullName(),
                  requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))));
        }
      }
    };
  }

  /**
   * Creates a condition that checks if a method does <strong>not</strong> contain
   * any of the specified modifiers.
   *
   * @param forbiddenModifiers the collection of modifiers that the method must not have
   * @return an architectural condition for checking that a method does not have the given modifiers
   */
  public static ArchCondition<JavaMethod> notHasMethodModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("does not have method modifiers") {
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
   * Creates a condition that checks if a class contains all the specified modifiers.
   *
   * @param requiredModifiers the collection of modifiers that the class is required to have
   * @return an architectural condition for checking if a class has the required modifiers
   */
  public static ArchCondition<JavaClass> hasClassModifiers(
      Collection<JavaModifier> requiredModifiers) {
    return new ArchCondition<>("has class modifiers") {
      @Override
      public void check(JavaClass clazz, ConditionEvents events) {
        if (!clazz.getModifiers().containsAll(requiredModifiers)) {
          events.add(SimpleConditionEvent.violated(clazz,
              "Class %s is missing one of these modifiers: %s".formatted(
                  clazz.getName(),
                  requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))));
        }
      }
    };
  }

  /**
   * Creates a condition that checks if a class does <strong>not</strong> contain
   * any of the specified modifiers.
   *
   * @param forbiddenModifiers the collection of modifiers that the class must not have
   * @return an architectural condition for checking that a class does not have the given modifiers
   */
  public static ArchCondition<JavaClass> notHasClassModifiers(
      Collection<JavaModifier> forbiddenModifiers) {
    return new ArchCondition<>("does not have class modifiers") {
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
    return modifiers.stream()
        .filter(forbiddenModifiers::contains)
        .sorted()
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }
}
