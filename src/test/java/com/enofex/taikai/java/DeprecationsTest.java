package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class DeprecationsTest {

  @Test
  void shouldPassWhenNoDeprecatedApisAreUsed() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(SafeClass.class))
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenCallingDeprecatedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(UsesDeprecatedMethod.class))
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenInstantiatingDeprecatedConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(UsesDeprecatedConstructor.class))
        .java(JavaConfigurer::noUsageOfDeprecatedAPIs)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenDependingOnDeprecatedClass() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(DependsOnDeprecatedClass.class))
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

  static class DeprecatedHolder {

    @Deprecated
    public static final String DEPRECATED_FIELD = "bad";

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

  static class DependsOnDeprecatedClass {

    DeprecatedClass reference;
  }
}
