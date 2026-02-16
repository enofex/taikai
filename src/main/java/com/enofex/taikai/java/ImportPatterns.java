package com.enofex.taikai.java;

/**
 * Predefined import patterns for commonly used Java and framework packages. These patterns can be
 * used with Taikai import rules such as:
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
   * Matches imports under {@code org.apache.commons}.
   */
  public static String apacheCommons() {
    return "org.apache.commons..";
  }

  /**
   * Matches imports under {@code org.assertj}.
   */
  public static String assertJ() {
    return "org.assertj..";
  }

  /**
   * Matches imports under {@code org.hamcrest}.
   */
  public static String hamcrest() {
    return "org.hamcrest..";
  }

  /**
   * Matches imports under {@code org.hibernate}.
   */
  public static String hibernate() {
    return "org.hibernate..";
  }

  /**
   * Matches imports under {@code org.jspecify}.
   */
  public static String jspecify() {
    return "org.jspecify..";
  }

  /**
   * Matches imports under {@code org.junit.jupiter} (JUnit 5 and higher).
   */
  public static String junit() {
    return "org.junit.jupiter..";
  }

  /**
   * Matches imports under {@code ch.qos.logback}.
   */
  public static String logback() {
    return "ch.qos.logback..";
  }

  /**
   * Matches imports under {@code lombok}.
   */
  public static String lombok() {
    return "lombok..";
  }

  /**
   * Matches imports under {@code org.mockito}.
   */
  public static String mockito() {
    return "org.mockito..";
  }

  /**
   * Matches shaded or relocated imports containing {@code .shaded.} in the package path.
   */
  public static String shaded() {
    return "..shaded..";
  }

  /**
   * Matches imports under {@code org.springframework.boot}.
   */
  public static String springBoot() {
    return "org.springframework.boot..";
  }

  /**
   * Matches imports under {@code org.springframework.data}.
   */
  public static String springData() {
    return "org.springframework.data..";
  }

  /**
   * Matches imports under {@code org.springframework}.
   */
  public static String springFramework() {
    return "org.springframework..";
  }

  /**
   * Matches imports under {@code org.springframework.security}.
   */
  public static String springSecurity() {
    return "org.springframework.security..";
  }

  /**
   * Matches imports under {@code org.testcontainers}.
   */
  public static String testcontainers() {
    return "org.testcontainers..";
  }
}