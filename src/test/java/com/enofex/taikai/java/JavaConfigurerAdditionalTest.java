package com.enofex.taikai.java;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JavaConfigurerAdditionalTest {

  @Nested
  class ClassesShouldResideInPackage {

    @Test
    void shouldNotThrowWhenClassResidingInExpectedPackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideInPackage(".*InPackageClass.*",
              "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNotResidingInExpectedPackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideInPackage(".*InPackageClass.*",
              "com.other.package"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideInPackage(".*InPackageClass.*",
              "com.enofex.taikai.java", TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldResideOutsidePackage {

    @Test
    void shouldNotThrowWhenClassResidingOutsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(".*InPackageClass.*",
              "com.other.package"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassResidingInForbiddenPackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(".*InPackageClass.*",
              "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(".*InPackageClass.*",
              "com.other.package", TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenClassHasAllRequiredAnnotations_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingRequiredAnnotations_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWithStringAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              MarkerAnnotation.class.getName(), List.of(Deprecated.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              MarkerAnnotation.class.getName(), List.of(Deprecated.class.getName()),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenClassHasAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ".*SingleAnnotatedClass.*", MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ".*InPackageClass.*", MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ".*SingleAnnotatedClass.*", MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassHasAnnotation_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ".*SingleAnnotatedClass.*", MarkerAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ".*SingleAnnotatedClass.*", MarkerAnnotation.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenClassLacksAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ".*InPackageClass.*", MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassHasForbiddenAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ".*SingleAnnotatedClass.*", MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ".*InPackageClass.*", MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassLacksAnnotation_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ".*InPackageClass.*", MarkerAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ".*InPackageClass.*", MarkerAnnotation.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesAnnotatedWithShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenAnnotatedClassDoesNotHaveForbiddenAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassHasForbiddenAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassDoesNotHaveForbiddenAnnotation_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class.getName(), Deprecated.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class.getName(), Deprecated.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesAnnotatedWithShouldResideInPackage {

    @Test
    void shouldNotThrowWhenAnnotatedClassResidingInExpectedPackage_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldResideInPackage(
              MarkerAnnotation.class, "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassNotResidingInExpectedPackage_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldResideInPackage(
              MarkerAnnotation.class, "com.other.package"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldResideInPackage(
              MarkerAnnotation.class, "com.enofex.taikai.java",
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassResidingInExpectedPackage_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldResideInPackage(
              MarkerAnnotation.class.getName(), "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesAnnotatedWithShouldResideInPackage(
              MarkerAnnotation.class.getName(), "com.enofex.taikai.java",
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAssignableTo {

    @Test
    void shouldNotThrowWhenClassAssignable_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo("ConcreteImpl", BaseInterface.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNotAssignable_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldBeAssignableTo("InPackageClass", BaseInterface.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassType() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo("ConcreteImpl", BaseInterface.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassAssignable_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo("ConcreteImpl",
              BaseInterface.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringType() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo("ConcreteImpl",
              BaseInterface.class.getName(), TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotDeclareException {

    @Test
    void shouldNotThrowWhenMethodDoesNotDeclareException_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCleanMethod.class)
          .java(java -> java.methodsShouldNotDeclareException("doWork", RuntimeException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDeclaresException_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDeclaredException.class)
          .java(java -> java.methodsShouldNotDeclareException("doWork", Exception.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassException() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCleanMethod.class)
          .java(java -> java.methodsShouldNotDeclareException("doWork", RuntimeException.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenMethodHasAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("markedMethod", MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodMissingAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("unannotatedMethod",
              MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("markedMethod", MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenMethodHasAllAnnotations_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodMissingOneAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MarkerAnnotation.class, List.of(Deprecated.class),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenMethodDoesNotHaveForbiddenAnnotation_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodHasBothAnnotations_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              MarkerAnnotation.class, Deprecated.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedMethodHasRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodMissingModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedMethodDoesNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodHasForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedFieldHasRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(PRIVATE)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldMissingModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithPublicAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(PRIVATE)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              MarkerAnnotation.class, List.of(PRIVATE),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedFieldDoesNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldHasForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              MarkerAnnotation.class, List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NoUsageOf {

    @Test
    void shouldNotThrowWhenClassNotUsed_WithPackageIdentifier() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class, "com.example.other"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsed_ClassWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsed_StringWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsed_ClassWithPackageAndConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class, "com.example.other",
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenForbiddenClassUsedInPackage() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingString.class)
          .java(java -> java.noUsageOf(String.class, "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableJavaConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ViolatingClass.class)
          .java(java -> java
              .utilityClassesShouldBeFinalAndHavePrivateConstructor()
              .disable())
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface MarkerAnnotation {

  }

  static class InPackageClass {

  }

  @MarkerAnnotation
  static class SingleAnnotatedClass {

  }

  @MarkerAnnotation
  @Deprecated
  static class DoublyAnnotatedClass {

  }

  interface BaseInterface {

  }

  static class ConcreteImpl implements BaseInterface {

  }

  static class ClassWithCleanMethod {

    void doWork() {
    }
  }

  static class ClassWithDeclaredException {

    void doWork() throws Exception {
    }
  }

  static class ClassWithAnnotatedMethod {

    @MarkerAnnotation
    void markedMethod() {
    }

    void unannotatedMethod() {
    }
  }

  static class ClassWithDoublyAnnotatedMethod {

    @MarkerAnnotation
    @Deprecated
    void markedMethod() {
    }
  }

  static class ClassWithStaticAnnotatedMethod {

    @MarkerAnnotation
    static void markedMethod() {
    }
  }

  static class ClassWithAnnotatedField {

    @MarkerAnnotation
    private String markedField = "value";
  }

  static class ClassWithPublicAnnotatedField {

    @MarkerAnnotation
    public String markedField = "value";
  }

  static class ClassWithStaticAnnotatedField {

    @MarkerAnnotation
    static String markedField = "value";
  }

  static class ClassUsingString {

    private String value = "test";
  }

  static class ViolatingClass {

    public void notAUtilityMethod() {
    }
  }
}
