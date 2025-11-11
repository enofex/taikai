package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodModifierTest {

  @Nested
  class MethodsAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAllAnnotatedMethodsHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodsLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(NonPublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedMethodsHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodsLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(NonPublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedMethodsDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedMethodsDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsMatchRegexAndHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldHaveModifiers(".*", List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsMatchRegexButDoNotHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MixedModifiers.class)
          .java(java -> java.methodsShouldHaveModifiers("one|two|three", List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(".*", List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(".*anotherPublicMethod", List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers("noSuchMethod", List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(".*", Set.of()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }


  @Nested
  class MethodsShouldHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenAllMethodsInClassHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(".*PublicMethods", List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassLacksRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MixedModifiers.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(".*MixedModifiers", List.of(JavaModifier.PRIVATE)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenAllMethodsInClassDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*PublicMethods", List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassHasForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*StaticMethods", List.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyClass() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*NonExistentClass", List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*StaticMethods", Set.of()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }



  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(PublicMethods.class)
        .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
            TestAnnotation.class, Set.of()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {
  }

  static class PublicMethods {

    @TestAnnotation
    public void publicMethod() {}

    public void anotherPublicMethod() {}
  }

  static class NonPublicMethods {

    @TestAnnotation
    void packagePrivateMethod() {}

    @TestAnnotation
    protected void protectedMethod() {}
  }

  static class StaticMethods {

    @TestAnnotation
    public static void staticMethod() {}

    public static void anotherStaticMethod() {}
  }

  static class MixedModifiers {

    public void one() {}
    protected void two() {}
    private static void three() {}
  }
}
