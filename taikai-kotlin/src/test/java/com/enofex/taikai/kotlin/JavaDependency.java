package com.enofex.taikai.kotlin;

/** A dependency used only by a compiler-generated default-argument helper. */
public final class JavaDependency {
  private JavaDependency() {
  }

  public static String value() {
    return "default";
  }
}
