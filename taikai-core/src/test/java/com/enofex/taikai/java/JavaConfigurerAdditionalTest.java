package com.enofex.taikai.java;

import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.MethodCriteria;
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
          .java(java -> java.classesShouldResideInPackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
              "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNotResidingInExpectedPackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideInPackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
              "com.other.package"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideInPackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
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
          .java(java -> java.classesShouldResideOutsidePackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
              "com.other.package"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassResidingInForbiddenPackage() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
              "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(ClassCriteria.nameMatching(".*InPackageClass.*"),
              "com.other.package", TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenClassHasAllRequiredAnnotationsUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingRequiredAnnotationsUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName()),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWithStringAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), List.of(Deprecated.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), List.of(Deprecated.class.getName()),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenClassHasAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*SingleAnnotatedClass.*"), MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*InPackageClass.*"), MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*SingleAnnotatedClass.*"), MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassHasAnnotationUsingTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*SingleAnnotatedClass.*"), MarkerAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*SingleAnnotatedClass.*"), MarkerAnnotation.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenClassLacksAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.nameMatching(".*InPackageClass.*"), MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassHasForbiddenAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.nameMatching(".*SingleAnnotatedClass.*"), MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.nameMatching(".*InPackageClass.*"), MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassLacksAnnotationUsingTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.nameMatching(".*InPackageClass.*"), MarkerAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.nameMatching(".*InPackageClass.*"), MarkerAnnotation.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotBeAnnotatedWithByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedClassDoesNotHaveForbiddenAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassHasForbiddenAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(DoublyAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassDoesNotHaveForbiddenAnnotationUsingTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), Deprecated.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), Deprecated.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldResideInPackageByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedClassResidingInExpectedPackageUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassNotResidingInExpectedPackageUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), "com.other.package"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(MarkerAnnotation.class), "com.enofex.taikai.java",
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassResidingInExpectedPackageUsingTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), "com.enofex.taikai.java"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(SingleAnnotatedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(MarkerAnnotation.class.getName()), "com.enofex.taikai.java",
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAssignableTo {

    @Test
    void shouldNotThrowWhenClassAssignableUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*ConcreteImpl"), BaseInterface.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNotAssignableUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*InPackageClass"), BaseInterface.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassType() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*ConcreteImpl"), BaseInterface.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassAssignableUsingTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*ConcreteImpl"),
              BaseInterface.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithStringType() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteImpl.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*ConcreteImpl"),
              BaseInterface.class.getName(), TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotDeclareException {

    @Test
    void shouldNotThrowWhenMethodDoesNotDeclareExceptionUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCleanMethod.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching("doWork"), RuntimeException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodDeclaresExceptionUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDeclaredException.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching("doWork"), Exception.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassException() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCleanMethod.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching("doWork"), RuntimeException.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenMethodHasAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("markedMethod"), MarkerAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodMissingAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("unannotatedMethod"),
              MarkerAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("markedMethod"), MarkerAnnotation.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenMethodHasAllAnnotationsUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodMissingOneAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(Deprecated.class.getName()),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotBeAnnotatedWithByAnnotation {

    @Test
    void shouldNotThrowWhenMethodDoesNotHaveForbiddenAnnotationUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotBeAnnotatedWith(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodHasBothAnnotationsUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithDoublyAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotBeAnnotatedWith(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotBeAnnotatedWith(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), Deprecated.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedMethodHasRequiredModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodMissingModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedMethodDoesNotHaveForbiddenModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodHasForbiddenModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.methodsShouldNotHaveModifiers(
              MethodCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedFieldHasRequiredModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsShouldHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(PRIVATE)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldMissingModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithPublicAnnotatedField.class)
          .java(java -> java.fieldsShouldHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(PRIVATE)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsShouldHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(PRIVATE),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldNotHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedFieldDoesNotHaveForbiddenModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldHasForbiddenModifiersUsingClass() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAnnotatedField.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(
              FieldCriteria.annotatedWith(MarkerAnnotation.class), List.of(STATIC),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NoUsageOf {

    @Test
    void shouldNotThrowWhenClassNotUsedWithPackageIdentifier() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.example.other"), String.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsedUsingClassWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsedUsingTypeNameWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(String.class.getName(),
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNotUsedUsingClassWithPackageAndConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InPackageClass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.example.other"), String.class,
              TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenForbiddenClassUsedInPackage() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingString.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.enofex.taikai.java"), String.class))
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
