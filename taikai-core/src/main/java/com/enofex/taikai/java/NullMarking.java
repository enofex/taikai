package com.enofex.taikai.java;

import static java.util.Objects.requireNonNull;

/**
 * The annotations that mark a scope as null-marked and that opt a scope out again, used by
 * {@link JavaConfigurer#classesShouldBeNullMarked(NullMarking)}.
 *
 * <p>A class, a method or constructor, or a package ({@code package-info.java}) is a scope. The
 * closest scope that is annotated with one of the two annotations decides whether a class is
 * null-marked, and a scope annotated with both is opted out. The annotations are given by name, so
 * they do not have to be on the classpath of Taikai.</p>
 *
 * <pre>{@code
 * .java(java -> java
 *     .classesShouldBeNullMarked(NullMarking.annotations(
 *         "com.example.NonNullByDefault", "com.example.NullUnmarked")))
 * }</pre>
 */
public final class NullMarking {

  private static final NullMarking JSPECIFY = new NullMarking(
      "org.jspecify.annotations.NullMarked", "org.jspecify.annotations.NullUnmarked");

  private final String marked;
  private final String unmarked;

  private NullMarking(String marked, String unmarked) {
    this.marked = requireNonNull(marked, "marked must not be null");
    this.unmarked = requireNonNull(unmarked, "unmarked must not be null");
  }

  /**
   * Returns the JSpecify annotations {@code org.jspecify.annotations.NullMarked} and
   * {@code org.jspecify.annotations.NullUnmarked}.
   *
   * @return the JSpecify null marking
   */
  public static NullMarking jspecify() {
    return JSPECIFY;
  }

  /**
   * Returns a null marking with the given annotations.
   *
   * @param marked   the fully qualified name of the annotation that marks a scope as null-marked
   * @param unmarked the fully qualified name of the annotation that opts a scope out again
   * @return the null marking
   */
  public static NullMarking annotations(String marked, String unmarked) {
    return new NullMarking(marked, unmarked);
  }

  String marked() {
    return this.marked;
  }

  String unmarked() {
    return this.unmarked;
  }
}
