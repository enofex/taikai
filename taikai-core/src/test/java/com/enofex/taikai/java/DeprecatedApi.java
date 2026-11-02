package com.enofex.taikai.java;

/**
 * Deprecated members for {@link DeprecationsTest}. They are declared in a top-level class of their
 * own, because uses within the same top-level class do not count, as for javac.
 */
final class DeprecatedApi {

  private DeprecatedApi() {
  }

  static class DeprecatedHolder {

    @Deprecated
    public static final String DEPRECATED_FIELD = "bad";

    @Deprecated
    public static Object DEPRECATED_OBJECT = new Object();

    @Deprecated
    public void deprecatedMethod() {
    }

    @Deprecated
    public DeprecatedHolder() {
    }
  }

  @Deprecated
  static class DeprecatedClass {

  }
}
