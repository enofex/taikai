package com.enofex.taikai.java;

import static com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static com.tngtech.archunit.core.domain.JavaModifier.SYNTHETIC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JavaCriteriaRulesTest {

  private static final String THIS_PACKAGE = "com.enofex.taikai.java";

  @Nested
  class ClassesShouldResideInPackage {

    @Test
    void shouldNotThrowWhenSelectedClassesResideInPackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(Marker.class), THIS_PACKAGE))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesResideElsewhere() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(Marker.class), "com.company.other.."))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("annotated with " + Marker.class.getName()));
    }

    @Test
    void shouldIgnoreClassesNotMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClass.class)
          .java(java -> java.classesShouldResideInPackage(
              ClassCriteria.annotatedWith(Marker.class), "com.company.other.."))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldResideOutsidePackage {

    @Test
    void shouldNotThrowWhenSelectedClassesResideOutsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(
              ClassCriteria.annotatedWith(Marker.class), "com.company.domain.."))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesResideInsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(
              ClassCriteria.annotatedWith(Marker.class), "com.enofex.taikai.."))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeRecords {

    @Test
    void shouldNotThrowWhenSelectedClassesAreRecords() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedRecord.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotRecords() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedRecord.class, MarkedClass.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(MarkedClass.class.getName()));
    }

    @Test
    void shouldHonorConfiguredClasses() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.annotatedWith(Marker.class),
              Configuration.of(new ClassFileImporter().importClasses(MarkedRecord.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeInterfaces {

    @Test
    void shouldNotThrowWhenSelectedClassesAreInterfaces() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedInterface.class)
          .java(java -> java.classesShouldBeInterfaces(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotInterfaces() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBeInterfaces(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenSelectedClassesCarryAnnotationClass() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"),
              Required.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesMissAnnotationName() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class, UnannotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"),
              Required.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UnannotatedApi.class.getName()));
    }

    @Test
    void shouldCombineCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class, UnannotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWith(
              ClassCriteria.nameMatching(".*Api").and(ClassCriteria.annotatedWith(Marker.class)),
              Required.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenSelectedClassesDoNotCarryAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedApi.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"),
              Required.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesCarryAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class)
          .java(java -> java.classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"),
              Required.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenSelectedClassesCarryAllAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(ClassCriteria.nameMatching(".*Api"),
              List.of(Marker.class.getName(), Required.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesMissAnAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(ClassCriteria.annotatedWith(Marker.class),
              List.of(Marker.class.getName(), Required.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAssignableTo {

    @Test
    void shouldNotThrowWhenSelectedClassesAreAssignable() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*Repository"),
              BaseRepository.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotAssignable() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class, OrderRepository.class)
          .java(java -> java.classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*Repository"),
              BaseRepository.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(OrderRepository.class.getName()));
    }
  }

  @Nested
  class ClassesShouldImplement {

    @Test
    void shouldNotThrowWhenSelectedClassesImplementInterface() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .java(java -> java.classesShouldImplement(ClassCriteria.nameMatching(".*Repository"),
              BaseRepository.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesDoNotImplementInterface() {
      Taikai taikai = Taikai.builder()
          .classes(OrderRepository.class)
          .java(java -> java.classesShouldImplement(ClassCriteria.nameMatching(".*Repository"),
              BaseRepository.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(FinalMarkedClass.class)
          .java(java -> java.classesShouldHaveModifiers(ClassCriteria.annotatedWith(Marker.class),
              List.of(FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldHaveModifiers(ClassCriteria.annotatedWith(Marker.class),
              List.of(FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(ClassCriteria.annotatedWith(Marker.class),
              List.of(FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(FinalMarkedClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(ClassCriteria.annotatedWith(Marker.class),
              List.of(FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenSelectedClassesArePackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesArePublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMarkedClass.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.annotatedWith(Marker.class)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenMethodsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class, MarkedClass.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PublicMethods"), List.of(PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenMethodsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PublicMethods"), List.of(PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class FieldsShouldHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, PackagePrivateField.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PrivateFinalFields"), List.of(PRIVATE, FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, PackagePrivateField.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(PRIVATE, FINAL)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PackagePrivateField.class.getName()));
    }

    @Test
    void shouldIgnoreSyntheticFields() {
      assertTrue(new ClassFileImporter().importClass(PrivateFinalFields.class).getFields().stream()
          .anyMatch(field -> field.getModifiers().contains(SYNTHETIC)));

      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(PRIVATE)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PrivateFinalFields"), List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(STATIC)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("counter"));
    }

    @Test
    void shouldHonorConfiguredClasses() {
      Taikai taikai = Taikai.builder()
          .classes(StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              ClassCriteria.annotatedWith(Marker.class), List.of(STATIC),
              Configuration.of(new ClassFileImporter().importClasses(PrivateFinalFields.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWithForClass {

    @Test
    void shouldNotThrowWhenAllMethodsOfSelectedClassesAreAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedMethods.class, PartlyAnnotatedMethods.class)
          .java(java -> java.methodsShouldBeAnnotatedWithForClass(
              ClassCriteria.simpleNameMatching("AnnotatedMethods"), Required.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAMethodOfSelectedClassesIsNotAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedMethods.class, PartlyAnnotatedMethods.class)
          .java(java -> java.methodsShouldBeAnnotatedWithForClass(
              ClassCriteria.nameMatching(".*AnnotatedMethods"), Required.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("unannotated()"));
    }

    @Test
    void shouldCheckPrivateMethodsToo() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateHelperMethods.class)
          .java(java -> java.methodsShouldBeAnnotatedWithForClass(
              ClassCriteria.simpleNameMatching("PrivateHelperMethods"), Required.class))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("helper()"));
    }
  }

  @Nested
  class MethodsShouldNotDeclareExceptionForClass {

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesDeclareException() {
      Taikai taikai = Taikai.builder()
          .classes(CallableTask.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("CallableTask"), IOException.class))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("call()"));
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesDeclareExceptionByName() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("ThrowingMethods"), SQLException.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("query()"));
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesDeclareExceptionMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("ThrowingMethods"), ClassCriteria.inPackage("java.sql")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoMatchingExceptionIsDeclared() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("ThrowingMethods"), ClassCriteria.inPackage("java.net")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldIgnoreClassesNotMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.inPackage("com.company.service.."), IOException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldIgnoreSyntheticBridgeMethods() {
      assertTrue(new ClassFileImporter().importClass(CallableTask.class).getMethods().stream()
          .anyMatch(method -> method.getModifiers().contains(SYNTHETIC)
              && method.getThrowsClause().containsType(Exception.class)));

      Taikai taikai = Taikai.builder()
          .classes(CallableTask.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("CallableTask"), Exception.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotDeclareException {

    @Test
    void shouldNotThrowWhenNoMatchingExceptionIsDeclared() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching("load"),
              ClassCriteria.simpleNameMatching("SQLException")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingExceptionIsDeclared() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
              ClassCriteria.inPackage("java.io").or(ClassCriteria.inPackage("java.sql"))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("load"));
      assertTrue(failure.getMessage().contains("query"));
    }
  }

  @Nested
  class NoUsageOf {

    @Test
    void shouldThrowWhenSelectedClassesUseClass() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("DateUser"), Date.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenUnselectedClassesUseTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.service.."),
              Date.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenTypesMatchingCriteriaAreUsed() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Date")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(Date.class.getName()));
    }

    @Test
    void shouldNotThrowWhenNoTypesMatchingCriteriaAreUsed() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("java.sql..")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldRestrictTypesMatchingCriteriaToSelectedClasses() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java
              .noUsageOf(ClassCriteria.annotatedWith(Marker.class), ClassCriteria.simpleNameMatching("Date")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesUseTypesMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java
              .noUsageOf(ClassCriteria.simpleNameMatching("DateUser"), ClassCriteria.inPackage("java.util")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowTypesMatchingCriteriaToUseEachOther() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Legacy.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenOtherClassesUseTypesMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class, LegacyClient.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Legacy(Api|Support)")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LegacyClient.class.getName() + ".api"));
      assertFalse(failure.getMessage().contains(LegacyApi.class.getName() + ".support"));
      assertFalse(failure.getMessage().contains(LegacySupport.class.getName() + ".api"));
    }

    @Test
    void shouldNotCheckSelectedClassesThatAreForbiddenThemselves() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class, LegacyClient.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Legacy.*"),
              ClassCriteria.simpleNameMatching("Legacy(Api|Support)")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LegacyClient.class.getName() + ".api"));
      assertFalse(failure.getMessage().contains(LegacyApi.class.getName() + ".support"));
      assertFalse(failure.getMessage().contains(LegacySupport.class.getName() + ".api"));
    }
  }

  @Nested
  class Imports {

    @Test
    void shouldThrowWhenClassesImportForbiddenTypes() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.inPackage("java.util"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenUnselectedClassesImportForbiddenTypes() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.annotatedWith(Marker.class),
                  ClassCriteria.inPackage("java.util"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesImportForbiddenTypes() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.simpleNameMatching("DateUser"),
                  ClassCriteria.simpleNameMatching("Date"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowForbiddenTypesToImportEachOther() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.simpleNameMatching("Legacy.*"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenOtherClassesImportForbiddenTypes() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class, LegacyClient.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.simpleNameMatching("Legacy(Api|Support)"))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LegacyClient.class.getName() + ".api"));
      assertFalse(failure.getMessage().contains(LegacyApi.class.getName() + ".support"));
      assertFalse(failure.getMessage().contains(LegacySupport.class.getName() + ".api"));
    }

    @Test
    void shouldNotCheckSelectedClassesThatAreForbiddenThemselves() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyApi.class, LegacySupport.class, LegacyClient.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(ClassCriteria.simpleNameMatching("Legacy.*"),
                  ClassCriteria.simpleNameMatching("Legacy(Api|Support)"))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LegacyClient.class.getName() + ".api"));
      assertFalse(failure.getMessage().contains(LegacyApi.class.getName() + ".support"));
      assertFalse(failure.getMessage().contains(LegacySupport.class.getName() + ".api"));
    }

    @Test
    void shouldNotThrowWhenSelectedClassesImportRequiredTypes() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.imports(imports -> imports
              .shouldImport(ClassCriteria.simpleNameMatching("DateUser"),
                  ClassCriteria.inPackage("java.util"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesDoNotImportRequiredTypes() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClass.class)
          .java(java -> java.imports(imports -> imports
              .shouldImport(ClassCriteria.simpleNameMatching("PlainClass"),
                  ClassCriteria.inPackage("java.util"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class Naming {

    @Test
    void shouldNotThrowWhenSelectedClassesMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class, PlainClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ClassCriteria.inPackage(THIS_PACKAGE).and(ClassCriteria.simpleNameMatching("User.*")),
              ".*Repository")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesDoNotMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ClassCriteria.annotatedWith(Marker.class), ".*Entity")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenSelectedClassesDoNotMatchForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(
              ClassCriteria.annotatedWith(Marker.class), ".*Impl")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesMatchForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(
              ClassCriteria.annotatedWith(Marker.class), ".*Class")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodsOfSelectedClassesMatchName() {
      assertTrue(new ClassFileImporter().importClass(FixtureStatus.class).getMethods().stream()
          .anyMatch(method -> method.getModifiers().contains(SYNTHETIC)));

      Taikai taikai = Taikai.builder()
          .classes(FixtureStatus.class, SnakeCaseMethods.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatchForClass(
              ClassCriteria.simpleNameMatching("FixtureStatus"), "[a-z][a-zA-Z0-9]*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesDoNotMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(SnakeCaseMethods.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatchForClass(
              ClassCriteria.simpleNameMatching("SnakeCaseMethods"), "[a-z][a-zA-Z0-9]*")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("do_work"));
    }

    @Test
    void shouldNotThrowWhenMethodsOfSelectedClassesAvoidForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(FixtureStatus.class)
          .java(java -> java.naming(naming -> naming.methodsShouldNotMatchForClass(
              ClassCriteria.simpleNameMatching("FixtureStatus"), ".*\\$.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsOfSelectedClassesHaveForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(SnakeCaseMethods.class, FixtureStatus.class)
          .java(java -> java.naming(naming -> naming.methodsShouldNotMatchForClass(
              ClassCriteria.simpleNameMatching("SnakeCaseMethods"), ".*_.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenFieldsOfSelectedTypesMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.ofType(ClassCriteria.inPackage("java.util")), ".*Date")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedTypesDoNotMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.ofType(ClassCriteria.simpleNameMatching("Date")), "timestamp")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesMatchName() {
      assertTrue(new ClassFileImporter().importClass(PrivateFinalFields.class).getFields().stream()
          .anyMatch(field -> field.getModifiers().contains(SYNTHETIC)));

      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatchForClass(
              ClassCriteria.simpleNameMatching("PrivateFinalFields"), "name")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesDoNotMatchName() {
      Taikai taikai = Taikai.builder()
          .classes(StaticField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatchForClass(
              ClassCriteria.annotatedWith(Marker.class), "name")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("counter"));
    }

    @Test
    void shouldSelectFieldsByDeclaringClassRatherThanByType() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatchForClass(
              ClassCriteria.simpleNameMatching("Date"), "timestamp")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesAvoidForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatchForClass(
              ClassCriteria.annotatedWith(Marker.class), ".*\\$.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesHaveForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateField.class, DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatchForClass(
              ClassCriteria.annotatedWith(Marker.class), "name|createdDate")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PackagePrivateField.class.getName()));
      assertFalse(failure.getMessage().contains(DateUser.class.getName()));
    }
  }

  @Nested
  class CombinedCriteria {

    @Test
    void shouldCheckOnlyMethodsMatchingAllCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(QueryMethods.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(Marker.class)
                  .and(MethodCriteria.nameMatching("find.*")),
              List.of(PUBLIC)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findHidden"));
      assertFalse(failure.getMessage().contains("save"));
      assertFalse(failure.getMessage().contains("findUnmarked"));
    }

    @Test
    void shouldSelectFieldsByType() {
      Taikai taikai = Taikai.builder()
          .classes(TypedFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(FieldCriteria.ofType(Date.class),
              List.of(PRIVATE, FINAL)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("Fields of type java.util.Date"));
      assertTrue(failure.getMessage().contains("mutableDate"));
      assertFalse(failure.getMessage().contains(".name"));
    }

    @Test
    void shouldSelectFieldsByAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(TypedFields.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(FieldCriteria.annotatedWith(Marker.class),
              List.of(STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotCheckTheSupertypeItself() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, UserRepository.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.assignableTo(BaseRepository.class), List.of(ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldCheckTheSubtypes() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, UserRepository.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.assignableTo(BaseRepository.class), List.of(ABSTRACT)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("violated (1 times)"));
      assertTrue(failure.getMessage().contains(
          "Class " + UserRepository.class.getName() + " is missing"));
    }
  }

  @Nested
  class SelectorVariants {

    @Test
    void shouldCheckReturnTypesOfMethodsMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(OptionalReturns.class)
          .java(java -> java.methodsShouldNotReturn(MethodCriteria.nameMatching("find.*"),
              ClassCriteria.type(Optional.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findOne"));
      assertFalse(failure.getMessage().contains("load"));
    }

    @Test
    void shouldRequireReturnTypesOfMethodsMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(OptionalReturns.class)
          .java(java -> java.methodsShouldReturn(MethodCriteria.nameMatching("find.*"),
              ClassCriteria.type(List.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findOne"));
      assertFalse(failure.getMessage().contains("findAll"));
    }

    @Test
    void shouldForbidAnnotationOnMethodsOfMatchingClasses() {
      Taikai taikai = Taikai.builder()
          .classes(QueryMethods.class, OptionalReturns.class)
          .java(java -> java.methodsShouldNotBeAnnotatedWithForClass(
              ClassCriteria.type(QueryMethods.class), Marker.class))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findAll"));
      assertFalse(failure.getMessage().contains("OptionalReturns"));
    }

    @Test
    void shouldRequireAnnotationsOnMethodsOfMatchingClasses() {
      Taikai taikai = Taikai.builder()
          .classes(QueryMethods.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAllForClass(
              ClassCriteria.type(QueryMethods.class), List.of(Marker.class.getName())))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findUnmarked"));
      assertFalse(failure.getMessage().contains("findHidden"));
    }

    @Test
    void shouldForbidMethodNamesForMethodsMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(QueryMethods.class)
          .java(java -> java.naming(naming -> naming.methodsShouldNotMatch(
              MethodCriteria.annotatedWith(Marker.class), "find.*")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findAll"));
      assertFalse(failure.getMessage().contains("findUnmarked"));
    }

    @Test
    void shouldForbidFieldNamesForFieldsMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(TypedFields.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatch(
              FieldCriteria.ofType(Date.class), "mutable.*")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("mutableDate"));
      assertFalse(failure.getMessage().contains("fixedDate"));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Marker {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Required {
  }

  @Marker
  static class MarkedClass {

    void run() {
    }
  }

  @Marker
  static final class FinalMarkedClass {
  }

  @Marker
  public static class PublicMarkedClass {
  }

  @Marker
  record MarkedRecord(String value) {
  }

  @Marker
  interface MarkedInterface {
  }

  @Marker
  @Required
  static class AnnotatedApi {
  }

  static class UnannotatedApi {
  }

  interface BaseRepository {
  }

  static class UserRepository implements BaseRepository {
  }

  static class OrderRepository {
  }

  static class PlainClass {
  }

  static class AnnotatedMethods {

    @Required
    void first() {
    }

    @Required
    private void helper() {
    }
  }

  static class PartlyAnnotatedMethods {

    @Required
    void first() {
    }

    void unannotated() {
    }
  }

  static class PrivateHelperMethods {

    @Required
    void first() {
      helper();
    }

    private void helper() {
    }
  }

  static class CallableTask implements Callable<String> {

    @Override
    public String call() throws IOException {
      return "done";
    }
  }

  enum FixtureStatus {
    ACTIVE
  }

  static class SnakeCaseMethods {

    void do_work() {
    }
  }

  @Marker
  static class PrivateFinalFields {

    private final String name = "fixture";

    String name() {
      assert this.name != null;
      return this.name;
    }
  }

  @Marker
  static class PackagePrivateField {

    String name;
  }

  @Marker
  static class StaticField {

    private static int counter;
  }

  static class PublicMethods {

    public void first() {
    }

    public void second() {
    }
  }

  static class ThrowingMethods {

    void load() throws IOException {
    }

    void query() throws SQLException {
    }
  }

  static class LegacyApi {

    private LegacySupport support;
  }

  static class LegacySupport {

    private LegacyApi api;
  }

  static class LegacyClient {

    private LegacyApi api;
  }

  static class DateUser {

    private Date createdDate;

    Date createdDate() {
      return this.createdDate;
    }
  }

  static class QueryMethods {

    @Marker
    public void findAll() {
    }

    @Marker
    void findHidden() {
    }

    @Marker
    void save() {
    }

    void findUnmarked() {
    }
  }

  static class TypedFields {

    private final Date fixedDate = new Date();

    Date mutableDate;

    @Marker
    private String name;
  }

  static class OptionalReturns {

    Optional<String> findOne() {
      return Optional.empty();
    }

    List<String> findAll() {
      return List.of();
    }

    Optional<String> load() {
      return Optional.empty();
    }
  }
}
