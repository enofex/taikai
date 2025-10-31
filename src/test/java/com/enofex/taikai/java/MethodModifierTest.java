package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodModifierTest {

  @Nested
  class MethodsAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAllAnnotatedMethodsHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodsLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonPublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedMethodsHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodsLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonPublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedMethodsDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedMethodsDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsMatchRegexAndHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsShouldHaveModifiers(".*", EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsMatchRegexButDoNotHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(MixedModifiers.class))
          .java(java -> java.methodsShouldHaveModifiers("one|two|three", EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiers(".*", EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiers(".*anotherPublicMethod", EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiers("noSuchMethod", EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticMethods.class))
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
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsShouldHaveModifiersForClass(".*PublicMethods", EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassLacksRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(MixedModifiers.class))
          .java(java -> java.methodsShouldHaveModifiersForClass(".*MixedModifiers", EnumSet.of(JavaModifier.PRIVATE)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenAllMethodsInClassDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*PublicMethods", EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassHasForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*StaticMethods", EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyClass() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*NonExistentClass", EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticMethods.class))
          .java(java -> java.methodsShouldNotHaveModifiersForClass(".*StaticMethods", Set.of()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }



  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(PublicMethods.class))
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
