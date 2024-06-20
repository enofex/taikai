package com.enofex.taikai.internal;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;

/**
 * This class provides utility methods for checking Java modifiers.
 * <p>
 * This class is intended for internal use only and is not part of the public API. Developers should
 * not rely on this class for any public API usage.
 */
public final class Modifiers {

  private Modifiers() {
  }

  /**
   * Checks if a class is final.
   *
   * @param javaClass the Java class to check
   * @return true if the class is final, false otherwise
   */
  public static boolean isClassFinal(JavaClass javaClass) {
    return javaClass.getModifiers().contains(JavaModifier.FINAL);
  }

  /**
   * Checks if a constructor is private.
   *
   * @param constructor the Java constructor to check
   * @return true if the constructor is private, false otherwise
   */
  public static boolean isConstructorPrivate(JavaConstructor constructor) {
    return constructor.getModifiers().contains(JavaModifier.PRIVATE);
  }

  /**
   * Checks if a method is protected.
   *
   * @param method the Java method to check
   * @return true if the method is protected, false otherwise
   */
  public static boolean isMethodProtected(JavaMethod method) {
    return method.getModifiers().contains(JavaModifier.PROTECTED);
  }

  /**
   * Checks if a method is static.
   *
   * @param method the Java method to check
   * @return true if the method is static, false otherwise
   */
  public static boolean isMethodStatic(JavaMethod method) {
    return method.getModifiers().contains(JavaModifier.STATIC);
  }

  /**
   * Checks if a field is static.
   *
   * @param field the Java field to check
   * @return true if the field is static, false otherwise
   */
  public static boolean isFieldStatic(JavaField field) {
    return field.getModifiers().contains(JavaModifier.STATIC);
  }

  /**
   * Checks if a field is public.
   *
   * @param field the Java field to check
   * @return true if the field is public, false otherwise
   */
  public static boolean isFieldPublic(JavaField field) {
    return field.getModifiers().contains(JavaModifier.PUBLIC);
  }

  /**
   * Checks if a field is protected.
   *
   * @param field the Java field to check
   * @return true if the field is protected, false otherwise
   */
  public static boolean isFieldProtected(JavaField field) {
    return field.getModifiers().contains(JavaModifier.PROTECTED);
  }

  /**
   * Checks if a field is final.
   *
   * @param field the Java field to check
   * @return true if the field is final, false otherwise
   */
  public static boolean isFieldFinal(JavaField field) {
    return field.getModifiers().contains(JavaModifier.FINAL);
  }

  /**
   * Checks if a field is synthetic.
   *
   * @param field the Java field to check
   * @return true if the field is synthetic, false otherwise
   */
  public static boolean isFieldSynthetic(JavaField field) {
    return field.getModifiers().contains(JavaModifier.SYNTHETIC);
  }
}
