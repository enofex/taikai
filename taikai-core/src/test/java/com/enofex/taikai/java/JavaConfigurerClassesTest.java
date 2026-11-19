package com.enofex.taikai.java;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.imported;
import static com.enofex.taikai.java.JavaChecks.overload;
import static com.enofex.taikai.java.JavaFixtures.MARKED;
import static com.enofex.taikai.java.JavaFixtures.THIS_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.FieldCriteria;
import com.enofex.taikai.core.TaikaiRule.Configuration;
import com.enofex.taikai.java.JavaFixtures.BaseRepository;
import com.enofex.taikai.java.JavaFixtures.Entity;
import com.enofex.taikai.java.JavaFixtures.MarkedClass;
import com.enofex.taikai.java.JavaFixtures.Marker;
import com.enofex.taikai.java.JavaFixtures.PlainClass;
import com.enofex.taikai.java.JavaFixtures.Required;
import com.enofex.taikai.java.JavaFixtures.UserRepository;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaConfigurerClassesTest {

  private static final DescribedPredicate<JavaClass> APIS = ClassCriteria.nameMatching(".*Api");
  private static final DescribedPredicate<JavaClass> REPOSITORIES =
      ClassCriteria.nameMatching(".*Repository");
  private static final DescribedPredicate<JavaClass> SERVICES =
      ClassCriteria.nameMatching(".*Service(Impl|WithoutInterface)");

  @Nested
  class ClassesShouldResideInPackage {

    @Test
    void shouldNotThrowWhenSelectedClassesResideInPackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideInPackage(MARKED, THIS_PACKAGE))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesResideElsewhere() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideInPackage(MARKED, "com.company.other.."))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("annotated with " + Marker.class.getName()));
    }

    @Test
    void shouldIgnoreClassesNotMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClass.class)
          .java(java -> java.classesShouldResideInPackage(MARKED, "com.company.other.."))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldResideInPackage(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldResideInPackage(MARKED, "com.company.other..",
                  defaultConfiguration()),
              PlainClass.class, MarkedClass.class));
    }
  }

  @Nested
  class ClassesShouldResideOutsidePackage {

    @Test
    void shouldNotThrowWhenSelectedClassesResideOutsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(MARKED, "com.company.domain.."))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesResideInsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldResideOutsidePackage(MARKED, "com.enofex.taikai.."))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldResideOutsidePackage(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldResideOutsidePackage(MARKED, THIS_PACKAGE,
                  defaultConfiguration()),
              PlainClass.class, MarkedClass.class));
    }
  }

  @Nested
  class ClassesShouldBeRecords {

    @Test
    void shouldNotThrowWhenClassNameMatchesAndIsRecord() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.nameMatching(".*ValidRecord")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameMatchesAndIsNotRecord() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNonRecord.class)
          .java(java -> java.classesShouldBeRecords(
              ClassCriteria.nameMatching(".*InvalidNonRecord")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotRecords() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedRecord.class, MarkedClass.class)
          .java(java -> java.classesShouldBeRecords(MARKED))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(MarkedClass.class.getName()));
    }

    @Test
    void shouldHonorConfiguredClasses() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBeRecords(MARKED,
              Configuration.of(imported(MarkedRecord.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeInterfaces {

    @Test
    void shouldNotThrowWhenMatchedTypeIsAnInterface() {
      Taikai taikai = Taikai.builder()
          .classes(MyRepository.class)
          .java(java -> java.classesShouldBeInterfaces(REPOSITORIES))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchedTypeIsNotAnInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteRepository.class)
          .java(java -> java.classesShouldBeInterfaces(REPOSITORIES))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldBeInterfaces(DescribedPredicate, Configuration)",
              java -> java.classesShouldBeInterfaces(REPOSITORIES, defaultConfiguration()),
              MyRepository.class, ConcreteRepository.class));
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenSelectedClassesCarryAllAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(APIS,
              List.of(Marker.class.getName(), Required.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesMissAnAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.classesShouldBeAnnotatedWithAll(MARKED,
              List.of(Marker.class.getName(), Required.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldBeAnnotatedWithAll(DescribedPredicate, Collection, Configuration)",
              java -> java.classesShouldBeAnnotatedWithAll(APIS,
                  List.of(Marker.class.getName(), Required.class.getName()),
                  defaultConfiguration()),
              AnnotatedApi.class, UnannotatedApi.class));
    }
  }

  @Nested
  class ClassesShouldHaveField {

    @Test
    void shouldPassWhenClassDeclaresRequiredField() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldFailWhenClassLacksRequiredField() {
      Taikai taikai = Taikai.builder()
          .classes(UnversionedEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UnversionedEntity.class.getName()));
      assertTrue(failure.getMessage().contains("does not have a field"));
    }

    @Test
    void shouldAcceptRequiredFieldInheritedFromSuperclass() {
      Taikai taikai = Taikai.builder()
          .classes(InheritingEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReportEachClassWithoutRequiredField() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class, UnversionedEntity.class, AnotherUnversionedEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UnversionedEntity.class.getName()));
      assertTrue(failure.getMessage().contains(AnotherUnversionedEntity.class.getName()));
      assertFalse(failure.getMessage().contains(VersionedEntity.class.getName()));
    }

    @Test
    void shouldIgnoreUnselectedClasses() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class, PlainClassWithField.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class.getName()),
              FieldCriteria.annotatedWith(Version.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldRequireAllCombinedFieldCriteriaOnTheSameField() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class, InstantVersionedEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class).and(FieldCriteria.ofType(Long.class))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(InstantVersionedEntity.class.getName()));
      assertFalse(failure.getMessage().contains(VersionedEntity.class.getName()));
    }

    @Test
    void shouldSelectRequiredFieldByName() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class, UnversionedEntity.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class), FieldCriteria.nameMatching("id")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldHonorConfigurationExclusions() {
      Taikai taikai = Taikai.builder()
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class),
              Configuration.of(
                  imported(VersionedEntity.class, UnversionedEntity.class,
                      AnotherUnversionedEntity.class),
                  List.of(UnversionedEntity.class.getName(),
                      AnotherUnversionedEntity.class.getName()))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldAllowNoMatchingClasses() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClassWithField.class)
          .java(java -> java.classesShouldHaveField(
              ClassCriteria.annotatedWith(Entity.class),
              FieldCriteria.annotatedWith(Version.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeAnnotatedWith {

    @Test
    void shouldThrowWhenSelectedClassesMissAnnotationName() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class, UnannotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWith(APIS, Required.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UnannotatedApi.class.getName()));
    }

    @Test
    void shouldCombineCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedApi.class, UnannotatedApi.class)
          .java(java -> java.classesShouldBeAnnotatedWith(APIS.and(MARKED), Required.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldBeAnnotatedWith(DescribedPredicate, Class)",
              java -> java.classesShouldBeAnnotatedWith(APIS, Required.class),
              AnnotatedApi.class, UnannotatedApi.class),
          overload("classesShouldBeAnnotatedWith(DescribedPredicate, Class, Configuration)",
              java -> java.classesShouldBeAnnotatedWith(APIS, Required.class,
                  defaultConfiguration()),
              AnnotatedApi.class, UnannotatedApi.class),
          overload("classesShouldBeAnnotatedWith(DescribedPredicate, String)",
              java -> java.classesShouldBeAnnotatedWith(APIS, Required.class.getName()),
              AnnotatedApi.class, UnannotatedApi.class),
          overload("classesShouldBeAnnotatedWith(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldBeAnnotatedWith(APIS, Required.class.getName(),
                  defaultConfiguration()),
              AnnotatedApi.class, UnannotatedApi.class));
    }
  }

  @Nested
  class ClassesShouldNotBeAnnotatedWith {

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldNotBeAnnotatedWith(DescribedPredicate, Class)",
              java -> java.classesShouldNotBeAnnotatedWith(APIS, Required.class),
              UnannotatedApi.class, AnnotatedApi.class),
          overload("classesShouldNotBeAnnotatedWith(DescribedPredicate, Class, Configuration)",
              java -> java.classesShouldNotBeAnnotatedWith(APIS, Required.class,
                  defaultConfiguration()),
              UnannotatedApi.class, AnnotatedApi.class),
          overload("classesShouldNotBeAnnotatedWith(DescribedPredicate, String)",
              java -> java.classesShouldNotBeAnnotatedWith(APIS, Required.class.getName()),
              UnannotatedApi.class, AnnotatedApi.class),
          overload("classesShouldNotBeAnnotatedWith(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldNotBeAnnotatedWith(APIS, Required.class.getName(),
                  defaultConfiguration()),
              UnannotatedApi.class, AnnotatedApi.class));
    }
  }

  @Nested
  class ClassesShouldBeAssignableTo {

    @Test
    void shouldThrowWhenSelectedClassesAreNotAssignable() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class, OrderRepository.class)
          .java(java -> java.classesShouldBeAssignableTo(REPOSITORIES,
              BaseRepository.class.getName()))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(OrderRepository.class.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldBeAssignableTo(DescribedPredicate, Class)",
              java -> java.classesShouldBeAssignableTo(REPOSITORIES, BaseRepository.class),
              UserRepository.class, OrderRepository.class),
          overload("classesShouldBeAssignableTo(DescribedPredicate, Class, Configuration)",
              java -> java.classesShouldBeAssignableTo(REPOSITORIES, BaseRepository.class,
                  defaultConfiguration()),
              UserRepository.class, OrderRepository.class),
          overload("classesShouldBeAssignableTo(DescribedPredicate, String)",
              java -> java.classesShouldBeAssignableTo(REPOSITORIES,
                  BaseRepository.class.getName()),
              UserRepository.class, OrderRepository.class),
          overload("classesShouldBeAssignableTo(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldBeAssignableTo(REPOSITORIES,
                  BaseRepository.class.getName(), defaultConfiguration()),
              UserRepository.class, OrderRepository.class));
    }
  }

  @Nested
  class ClassesShouldImplement {

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceImpl.class, MyService.class)
          .java(java -> java.classesShouldImplement(
              ClassCriteria.nameMatching(".*NonExistent"), MyService.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldImplement(DescribedPredicate, Class)",
              java -> java.classesShouldImplement(SERVICES, MyService.class),
              ServiceImpl.class, ServiceWithoutInterface.class),
          overload("classesShouldImplement(DescribedPredicate, Class, Configuration)",
              java -> java.classesShouldImplement(SERVICES, MyService.class,
                  defaultConfiguration()),
              ServiceImpl.class, ServiceWithoutInterface.class),
          overload("classesShouldImplement(DescribedPredicate, String)",
              java -> java.classesShouldImplement(SERVICES, MyService.class.getName()),
              ServiceImpl.class, ServiceWithoutInterface.class),
          overload("classesShouldImplement(DescribedPredicate, String, Configuration)",
              java -> java.classesShouldImplement(SERVICES, MyService.class.getName(),
                  defaultConfiguration()),
              ServiceImpl.class, ServiceWithoutInterface.class));
    }
  }

  @Nested
  class ClassesShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenClassHasAllRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingOneOrMoreRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicNonFinalClass"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenModifiersListIsEmpty() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              List.of())) // empty list → always allowed
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldCheckTheSubtypes() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, UserRepository.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.assignableTo(BaseRepository.class), List.of(JavaModifier.ABSTRACT)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("violated (1 times)"));
      assertTrue(failure.getMessage().contains(
          "Class " + UserRepository.class.getName() + " is missing"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.classesShouldHaveModifiers(
                  ClassCriteria.nameMatching(".*Public(Non)?FinalClass"),
                  List.of(JavaModifier.PUBLIC, JavaModifier.FINAL), defaultConfiguration()),
              PublicFinalClass.class, PublicNonFinalClass.class));
    }
  }

  @Nested
  class ClassesShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenClassDoesNotHaveDisallowedModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*PackagePrivateClass"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassHasAllDisallowedModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*DoesNotMatch"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotCheckTheSupertypeItself() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, UserRepository.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.assignableTo(BaseRepository.class), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldNotHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.classesShouldNotHaveModifiers(
                  ClassCriteria.nameMatching(".*(PackagePrivate|PublicFinal)Class"),
                  EnumSet.of(JavaModifier.PUBLIC), defaultConfiguration()),
              PackagePrivateClass.class, PublicFinalClass.class));
    }
  }

  @Nested
  class ClassesShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenMatchingClassIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Entity")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Entity")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldBePackagePrivate(DescribedPredicate, Configuration)",
              java -> java.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Entity"),
                  defaultConfiguration()),
              PackagePrivateEntity.class, PublicEntity.class));
    }
  }

  @Marker
  record MarkedRecord(String value) {
  }

  @Marker
  @Required
  static class AnnotatedApi {
  }

  static class UnannotatedApi {
  }

  static class OrderRepository {
  }

  record ValidRecord(String name, int id) { }

  static class InvalidNonRecord {
    private final String name = "test";
  }

  interface MyRepository {
    void findAll();
  }

  static class ConcreteRepository {
    public void findAll() {
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Version {
  }

  @Entity
  static class VersionedEntity {
    private Long id;

    @Version
    private Long version;
  }

  @Entity
  static class InstantVersionedEntity {
    private Long id;

    @Version
    private Instant version;
  }

  @Entity
  static class UnversionedEntity {
    private Long id;
  }

  @Entity
  static class AnotherUnversionedEntity {
    private Long id;
  }

  abstract static class VersionedBase {
    @Version
    private Long version;
  }

  @Entity
  static class InheritingEntity extends VersionedBase {
    private Long id;
  }

  static class PlainClassWithField {
    private String name;
  }

  interface MyService {
    void execute();
  }

  static class ServiceImpl implements MyService {
    @Override
    public void execute() {
      // Implementation here
    }
  }

  static class ServiceWithoutInterface {
    public void execute() {
      // No interface implemented
    }
  }

  public final class PublicFinalClass {}

  public class PublicNonFinalClass {}

  static class PackagePrivateClass {}

  static class PackagePrivateEntity {
  }

  public static class PublicEntity {
  }
}
