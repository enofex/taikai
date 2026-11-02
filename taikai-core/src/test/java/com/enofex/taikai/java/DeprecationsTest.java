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

class DeprecationsTest {

  @Test
  void shouldPassWhenNoDeprecatedApisAreUsed() {
    Taikai taikai = Taikai.builder()
        .classes(SafeClass.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenCallingDeprecatedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(UsesDeprecatedMethod.class)
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
  void shouldFailWhenInstantiatingDeprecatedConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(UsesDeprecatedConstructor.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
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
  void shouldPassWhenEnumDeclaresDeprecatedConstant() {
    Taikai taikai = Taikai.builder()
        .classes(Status.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldPassWhenRecordDeclaresDeprecatedComponent() {
    Taikai taikai = Taikai.builder()
        .classes(Order.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
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

  @Test
  void shouldFailWhenDependingOnDeprecatedClass() {
    Taikai taikai = Taikai.builder()
        .classes(DependsOnDeprecatedClass.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenAccessingDeprecatedField() {
    Taikai taikai = Taikai.builder()
        .classes(AccessesDeprecatedField.class)
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
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
