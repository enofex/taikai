package com.enofex.taikai.internal;

import static java.util.stream.Collectors.joining;

import com.tngtech.archunit.core.domain.AccessTarget.MethodCallTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.HasParameterTypes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Helpers for the structure of classes that several rules share.
 */
public final class Classes {

  private Classes() {
  }

  /**
   * Returns the outermost class that a class is nested in, or the class itself if it is a
   * top-level class.
   *
   * @param javaClass the class
   * @return the top-level class
   */
  public static JavaClass topLevelClass(JavaClass javaClass) {
    JavaClass topLevelClass = javaClass;

    while (topLevelClass.getEnclosingClass().isPresent()) {
      topLevelClass = topLevelClass.getEnclosingClass().get();
    }

    return topLevelClass;
  }

  /**
   * Returns the method calls and the method references of a class, such as
   * {@code orders.forEach(this::save)}, which use the referenced method just like a call does.
   *
   * @param javaClass the class
   * @return the method calls and references from the class
   */
  public static List<JavaCodeUnitAccess<?>> methodCallsAndReferences(JavaClass javaClass) {
    return Stream.concat(javaClass.getMethodCallsFromSelf().stream(),
            javaClass.getMethodReferencesFromSelf().stream())
        .toList();
  }

  /**
   * Returns the constructor calls and the constructor references of a class, such as
   * {@code Order::new}, which use the referenced constructor just like a call does.
   *
   * @param javaClass the class
   * @return the constructor calls and references from the class
   */
  public static List<JavaCodeUnitAccess<?>> constructorCallsAndReferences(JavaClass javaClass) {
    return Stream.concat(javaClass.getConstructorCallsFromSelf().stream(),
            javaClass.getConstructorReferencesFromSelf().stream())
        .toList();
  }

  /**
   * Returns the method calls and the method references of a method or constructor, which use the
   * referenced method just like a call does.
   *
   * @param codeUnit the method or constructor
   * @return the method calls and references from the code unit
   */
  public static List<JavaCodeUnitAccess<?>> methodCallsAndReferences(JavaCodeUnit codeUnit) {
    return Stream.concat(codeUnit.getMethodCallsFromSelf().stream(),
            codeUnit.getMethodReferencesFromSelf().stream())
        .toList();
  }

  /**
   * Returns the method that a bridge method calls: the method that overrides a method of a generic
   * type with other parameter types after erasure, such as {@code save(String)} for the bridge
   * method {@code save(Object)} that the compiler generates for it.
   *
   * @param bridge the bridge method
   * @return the method of the same name that the bridge method calls on the class or one of its
   *     supertypes, which declares the method when the class inherits it, or empty if there is
   *     none
   */
  public static Optional<MethodCallTarget> bridgeTarget(JavaMethod bridge) {
    return bridge.getMethodCallsFromSelf().stream()
        .map(JavaMethodCall::getTarget)
        .filter(target -> target.getName().equals(bridge.getName())
            && bridge.getOwner().isAssignableTo(target.getOwner().getName()))
        .findFirst();
  }

  /**
   * Returns the signature of a method after erasure, by which it overrides a method of the same
   * signature, together with {@link #bridgeTarget(JavaMethod)} for a method of a generic type.
   *
   * @param method the method, or the target of a call to it
   * @param <T> the type of the method
   * @return the name and the raw parameter types, such as {@code save(java.lang.String)}
   */
  public static <T extends HasName & HasParameterTypes> String signature(T method) {
    return method.getName() + method.getRawParameterTypes().stream()
        .map(JavaClass::getName)
        .collect(joining(",", "(", ")"));
  }
}
