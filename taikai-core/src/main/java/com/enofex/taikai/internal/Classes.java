package com.enofex.taikai.internal;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import java.util.List;
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
}
