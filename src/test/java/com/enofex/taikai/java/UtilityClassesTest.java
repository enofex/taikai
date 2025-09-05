package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class UtilityClassesTest {

  @Test
  void shouldApplyFinalUtilityClassWithPrivateConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ValidUtilityClass.class))
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyExceptionClassWithFactoryMethods() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ExceptionClassWithFactoryMethod.class))
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowUtilityClassThatIsNotFinal() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(NonFinalUtilityClass.class))
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowUtilityClassWithImplicitPublicConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(
            FinalUtilityClassWithImplicitPublicConstructor.class))
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowUtilityClassWithExplicitPublicConstructor() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(
            FinalUtilityClassWithExplicitPublicConstructor.class))
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  private static final class ValidUtilityClass {

    public static int number() {
      return 42;
    }

    private ValidUtilityClass() {
    }
  }

  private static class NonFinalUtilityClass {

    public static int number() {
      return 42;
    }

    private NonFinalUtilityClass() {
    }
  }

  /**
   * Class must be declared as public, otherwise the implicit constructor is private.
   */
  public static final class FinalUtilityClassWithImplicitPublicConstructor {

    public static int number() {
      return 42;
    }
  }

  private static final class FinalUtilityClassWithExplicitPublicConstructor {

    public static int number() {
      return 42;
    }

    public FinalUtilityClassWithExplicitPublicConstructor() {
    }
  }

  private static class ExceptionClassWithFactoryMethod extends RuntimeException {

    public static ExceptionClassWithFactoryMethod failure() {
      return new ExceptionClassWithFactoryMethod("Something went wrong.");
    }

    public ExceptionClassWithFactoryMethod(String message) {
      super(message);
    }
  }
}
