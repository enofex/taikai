package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.java.DeprecatedApi.DeprecatedClass;
import com.enofex.taikai.java.DeprecatedApi.DeprecatedHolder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeprecationsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {SafeClass.class, Status.class, Order.class})
  void shouldPassWhenNoDeprecatedApisOfOtherClassesAreUsed(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {UsesDeprecatedMethod.class, UsesDeprecatedConstructor.class,
      DependsOnDeprecatedClass.class, AccessesDeprecatedField.class})
  void shouldFailWhenUsingDeprecatedApis(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenCallingDeprecatedObjectMethod() {
    Taikai taikai = Taikai.builder()
        .classes(CallsDeprecatedObjectMethod.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);
    assertTrue(error.getMessage().contains("Method finalize"), error.getMessage());
  }

  @Test
  void shouldFailWhenReferencingDeprecatedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(ReferencesDeprecatedMethod.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("Method deprecatedMethod"), error.getMessage());
  }

  @Test
  void shouldFailWhenReferencingDeprecatedConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(ReferencesDeprecatedConstructor.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("Constructor"), error.getMessage());
  }

  @Test
  void shouldPassWhenClassUsesItsOwnDeprecatedMembers() {
    Taikai taikai = Taikai.builder()
        .classes(LegacyClient.class, LegacyClient.Builder.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenCallingInheritedDeprecatedMethodOfAnotherClass() {
    Taikai taikai = Taikai.builder()
        .classes(CallsInheritedDeprecatedMethod.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("Method deprecatedMethod"), error.getMessage());
  }

  static class SafeClass {

    String value;

    String method() {
      return "ok";
    }
  }

  enum Status {
    ACTIVE,
    @Deprecated
    LEGACY
  }

  record Order(String id, @Deprecated String legacyCode) {

  }

  @Deprecated
  static class LegacyClient {

    @Deprecated
    private int timeoutSeconds = 30;

    @Deprecated
    String legacyFormat() {
      return "legacy";
    }

    String format() {
      return legacyFormat() + this.timeoutSeconds;
    }

    static final class Builder {

      LegacyClient build() {
        return new LegacyClient();
      }
    }
  }

  static class CallsInheritedDeprecatedMethod extends DeprecatedHolder {

    void call() {
      deprecatedMethod();
    }
  }

  static class UsesDeprecatedMethod {

    void call() {
      new DeprecatedHolder().deprecatedMethod();
    }
  }

  static class UsesDeprecatedConstructor {

    void construct() {
      new DeprecatedHolder();
    }
  }

  static class ReferencesDeprecatedMethod {

    Consumer<DeprecatedHolder> reference() {
      return DeprecatedHolder::deprecatedMethod;
    }
  }

  static class ReferencesDeprecatedConstructor {

    Supplier<DeprecatedHolder> reference() {
      return DeprecatedHolder::new;
    }
  }

  static class DependsOnDeprecatedClass {

    DeprecatedClass reference;
  }

  static class AccessesDeprecatedField {

    Object value = DeprecatedHolder.DEPRECATED_OBJECT;
  }

  static class CallsDeprecatedObjectMethod {

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    protected void finalize() throws Throwable {
      super.finalize();
    }
  }
}
