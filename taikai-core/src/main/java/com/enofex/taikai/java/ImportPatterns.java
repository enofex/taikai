package com.enofex.taikai.java;

import com.enofex.taikai.ClassCriteria;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

/**
 * Predefined criteria for commonly used Java and framework packages, created with
 * {@link ClassCriteria#inPackage(String)}. They can be used wherever criteria are accepted, for example
 * with Taikai import rules:
 *
 * <pre>
 *   .imports(imports -> imports
 *       .shouldNotImport(lombok()))
 * </pre>
 */
public final class ImportPatterns {

  private ImportPatterns() {
  }

  /**
   * Matches classes under {@code org.apache.commons}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> apacheCommons() {
    return ClassCriteria.inPackage("org.apache.commons..");
  }

  /**
   * Matches classes under {@code org.assertj}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> assertJ() {
    return ClassCriteria.inPackage("org.assertj..");
  }

  /**
   * Matches classes under {@code org.hamcrest}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> hamcrest() {
    return ClassCriteria.inPackage("org.hamcrest..");
  }

  /**
   * Matches classes under {@code org.hibernate}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> hibernate() {
    return ClassCriteria.inPackage("org.hibernate..");
  }

  /**
   * Matches classes under {@code org.jspecify}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> jspecify() {
    return ClassCriteria.inPackage("org.jspecify..");
  }

  /**
   * Matches classes under {@code org.junit.jupiter} (JUnit 5 and higher).
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> junit() {
    return ClassCriteria.inPackage("org.junit.jupiter..");
  }

  /**
   * Matches classes under {@code ch.qos.logback}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> logback() {
    return ClassCriteria.inPackage("ch.qos.logback..");
  }

  /**
   * Matches classes under {@code lombok}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> lombok() {
    return ClassCriteria.inPackage("lombok..");
  }

  /**
   * Matches classes under {@code org.mockito}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> mockito() {
    return ClassCriteria.inPackage("org.mockito..");
  }

  /**
   * Matches shaded or relocated classes containing {@code .shaded.} in the package path.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> shaded() {
    return ClassCriteria.inPackage("..shaded..");
  }

  /**
   * Matches classes under {@code org.springframework.boot}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> springBoot() {
    return ClassCriteria.inPackage("org.springframework.boot..");
  }

  /**
   * Matches classes under {@code org.springframework.data}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> springData() {
    return ClassCriteria.inPackage("org.springframework.data..");
  }

  /**
   * Matches classes under {@code org.springframework}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> springFramework() {
    return ClassCriteria.inPackage("org.springframework..");
  }

  /**
   * Matches classes under {@code org.springframework.security}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> springSecurity() {
    return ClassCriteria.inPackage("org.springframework.security..");
  }

  /**
   * Matches classes under {@code org.testcontainers}.
   *
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> testcontainers() {
    return ClassCriteria.inPackage("org.testcontainers..");
  }
}
