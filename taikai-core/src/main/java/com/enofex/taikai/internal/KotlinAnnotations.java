package com.enofex.taikai.internal;

/**
 * The names of the annotations of Kotlin, which the rules and exports look for. They are names
 * rather than classes, so that Kotlin need not be on the classpath.
 */
public final class KotlinAnnotations {

  /**
   * The annotation that the Kotlin compiler puts on every class that it compiles.
   */
  public static final String ANNOTATION_METADATA = "kotlin.Metadata";

  private KotlinAnnotations() {
  }
}
