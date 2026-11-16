package com.enofex.taikai.java.fixtures.deprecations;

/**
 * Deprecated members for {@link com.enofex.taikai.java.DeprecationsTest}. They are declared in a
 * top-level class of their own, because uses within the same top-level class do not count, as for
 * javac.
 */
public final class DeprecatedApi {

  private DeprecatedApi() {
  }

  public static class DeprecatedHolder {

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
  public static class DeprecatedClass {

  }
}
