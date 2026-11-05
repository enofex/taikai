package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UtilityClassesTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {ValidUtilityClass.class, ExceptionClassWithFactoryMethod.class,
      InterfaceWithOnlyStaticMethods.class})
  void shouldPassForFinalUtilityClassesWithPrivateConstructorAndNonUtilityClasses(
      Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {NonFinalUtilityClass.class,
      FinalUtilityClassWithImplicitPublicConstructor.class,
      FinalUtilityClassWithExplicitPublicConstructor.class,
      FinalUtilityClassWithPrivateAndPublicConstructor.class})
  void shouldThrowForUtilityClassesThatAreNotFinalOrHaveNonPrivateConstructors(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::utilityClassesShouldBeFinalAndHavePrivateConstructor)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldDisableJavaConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(NonFinalUtilityClass.class)
        .java(java -> java
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .disable())
        .build();

    assertDoesNotThrow(taikai::check);
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

  private static final class FinalUtilityClassWithPrivateAndPublicConstructor {

    public static int number() {
      return 42;
    }

    private FinalUtilityClassWithPrivateAndPublicConstructor() {
    }

    public FinalUtilityClassWithPrivateAndPublicConstructor(int number) {
    }
  }

  private interface InterfaceWithOnlyStaticMethods {

    static int number() {
      return 42;
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
