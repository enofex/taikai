package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.namingOverload;
import static com.enofex.taikai.java.JavaFixtures.MARKED;
import static com.enofex.taikai.java.JavaFixtures.THIS_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaFixtures.DateUser;
import com.enofex.taikai.java.JavaFixtures.MarkedClass;
import com.enofex.taikai.java.JavaFixtures.Marker;
import com.enofex.taikai.java.JavaFixtures.PackagePrivateField;
import com.enofex.taikai.java.JavaFixtures.PlainClass;
import com.enofex.taikai.java.JavaFixtures.PrivateFinalFields;
import com.enofex.taikai.java.JavaFixtures.QueryMethods;
import com.enofex.taikai.java.JavaFixtures.StaticField;
import com.enofex.taikai.java.JavaFixtures.TypedFields;
import com.enofex.taikai.java.JavaFixtures.UserRepository;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NamingConfigurerTest {

  private static final DescribedPredicate<JavaClass> IN_THIS_PACKAGE =
      ClassCriteria.inPackage(THIS_PACKAGE);

  @Nested
  class PackagesShouldMatchDefault {

    @Test
    void shouldNotThrowWhenPackageMatchesDefaultPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(NamingConfigurer::packagesShouldMatchDefault))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationOverload() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.packagesShouldMatchDefault(
              defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class PackagesShouldMatch {

    @Test
    void shouldNotThrowWhenPackageMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.packagesShouldMatch("com\\.enofex\\..*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenPackageDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.packagesShouldMatch("org\\.enofex\\..*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldMatch {

    @Test
    void shouldNotThrowWhenClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(".*Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNamingClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(".*Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ClassCriteria.annotatedWith(Deprecated.class), ".*Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedInvalidClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ClassCriteria.annotatedWith(Deprecated.class), ".*Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

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
          .java(java -> java.naming(naming -> naming.classesShouldMatch(MARKED, ".*Entity")))
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
          namingOverload("classesShouldMatch(String, Configuration)",
              naming -> naming.classesShouldMatch(".*Service", defaultConfiguration()),
              ValidService.class, InvalidNamingClass.class),
          namingOverload("classesShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.classesShouldMatch(ClassCriteria.annotatedWith(Deprecated.class),
                  ".*Service", defaultConfiguration()),
              ValidService.class, AnnotatedInvalidClass.class));
    }
  }

  @Nested
  class ClassesShouldNotMatch {

    @Test
    void shouldNotThrowWhenClassNameDoesNotMatchForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(".*Impl")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameMatchesForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNamingClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(".*InvalidNamingClass")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenSelectedClassesDoNotMatchForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(MARKED, ".*Impl")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesMatchForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldNotMatch(MARKED, ".*Class")))
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
          namingOverload("classesShouldNotMatch(String, Configuration)",
              naming -> naming.classesShouldNotMatch(".*InvalidNamingClass",
                  defaultConfiguration()),
              ValidService.class, InvalidNamingClass.class),
          namingOverload("classesShouldNotMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.classesShouldNotMatch(MARKED, ".*Class", defaultConfiguration()),
              PrivateFinalFields.class, MarkedClass.class));
    }
  }

  @Nested
  class MethodsShouldMatch {

    @Test
    void shouldNotThrowWhenMethodNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCamelCaseMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithBadMethodName.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedMethodNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch(
              MethodCriteria.annotatedWith(Deprecated.class), "deprecated.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch(
              MethodCriteria.annotatedWith(Deprecated.class), "valid.*")))
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
          namingOverload("methodsShouldMatch(String, Configuration)",
              naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*", defaultConfiguration()),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class),
          namingOverload("methodsShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.methodsShouldMatch(MethodCriteria.nameMatching(".*Something"),
                  "[a-z][a-zA-Z0-9]*", defaultConfiguration()),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class));
    }
  }

  @Nested
  class MethodsShouldNotMatch {

    @Test
    void shouldNotThrowWhenMethodNameDoesNotMatchForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCamelCaseMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldNotMatch("^temp.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodNameMatchesForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithBadMethodName.class)
          .java(java -> java.naming(naming -> naming.methodsShouldNotMatch(".*Do_.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload("methodsShouldNotMatch(String, Configuration)",
              naming -> naming.methodsShouldNotMatch(".*Do_.*", defaultConfiguration()),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class),
          namingOverload("methodsShouldNotMatch(DescribedPredicate, String)",
              naming -> naming.methodsShouldNotMatch(MethodCriteria.nameMatching(".*Something"),
                  ".*_.*"),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class),
          namingOverload("methodsShouldNotMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.methodsShouldNotMatch(MethodCriteria.nameMatching(".*Something"),
                  ".*_.*", defaultConfiguration()),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class));
    }
  }

  @Nested
  class MethodsShouldMatchForClass {

    @Test
    void shouldNotThrowWhenMethodsOfSelectedClassesMatchName() {
      assertTrue(new ClassFileImporter().importClass(FixtureStatus.class).getMethods().stream()
          .anyMatch(method -> method.getModifiers().contains(JavaModifier.SYNTHETIC)));

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload("methodsShouldMatchForClass(DescribedPredicate, String, Configuration)",
              naming -> naming.methodsShouldMatchForClass(IN_THIS_PACKAGE, "[a-z][a-zA-Z0-9]*",
                  defaultConfiguration()),
              ClassWithCamelCaseMethod.class, SnakeCaseMethods.class));
    }
  }

  @Nested
  class MethodsShouldNotMatchForClass {

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload(
              "methodsShouldNotMatchForClass(DescribedPredicate, String, Configuration)",
              naming -> naming.methodsShouldNotMatchForClass(IN_THIS_PACKAGE, ".*_.*",
                  defaultConfiguration()),
              ClassWithCamelCaseMethod.class, SnakeCaseMethods.class));
    }
  }

  @Nested
  class FieldsShouldMatch {

    @Test
    void shouldNotThrowWhenFieldNameOfTypeMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.ofType(String.class), "my.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldNameOfTypeDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.ofType(String.class), "other.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllFieldNamesMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch("my.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch("other.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedFieldNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.annotatedWith(Deprecated.class), "deprecatedField")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              FieldCriteria.annotatedWith(Deprecated.class), "validField")))
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload("fieldsShouldMatch(String, Configuration)",
              naming -> naming.fieldsShouldMatch("my.*", defaultConfiguration()),
              ClassWithStringField.class, ClassWithAnnotatedField.class),
          namingOverload("fieldsShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.fieldsShouldMatch(FieldCriteria.ofType(String.class), "my.*",
                  defaultConfiguration()),
              ClassWithStringField.class, ClassWithAnnotatedField.class));
    }
  }

  @Nested
  class FieldsShouldNotMatch {

    @Test
    void shouldNotThrowWhenFieldNameDoesNotMatchForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatch("debug.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldNameMatchesForbiddenPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatch("my.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload("fieldsShouldNotMatch(String, Configuration)",
              naming -> naming.fieldsShouldNotMatch("my.*", defaultConfiguration()),
              ClassWithAnnotatedField.class, ClassWithStringField.class),
          namingOverload("fieldsShouldNotMatch(DescribedPredicate, String)",
              naming -> naming.fieldsShouldNotMatch(FieldCriteria.ofType(String.class), "my.*"),
              ClassWithAnnotatedField.class, ClassWithStringField.class),
          namingOverload("fieldsShouldNotMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.fieldsShouldNotMatch(FieldCriteria.ofType(String.class), "my.*",
                  defaultConfiguration()),
              ClassWithAnnotatedField.class, ClassWithStringField.class));
    }
  }

  @Nested
  class FieldsShouldMatchForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesMatchName() {
      assertTrue(new ClassFileImporter().importClass(PrivateFinalFields.class).getFields().stream()
          .anyMatch(field -> field.getModifiers().contains(JavaModifier.SYNTHETIC)));

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
          .java(java -> java.naming(naming -> naming.fieldsShouldMatchForClass(MARKED, "name")))
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload("fieldsShouldMatchForClass(DescribedPredicate, String, Configuration)",
              naming -> naming.fieldsShouldMatchForClass(MARKED, "name", defaultConfiguration()),
              PrivateFinalFields.class, StaticField.class));
    }
  }

  @Nested
  class FieldsShouldNotMatchForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesAvoidForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatchForClass(
              MARKED, ".*\\$.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesHaveForbiddenName() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateField.class, DateUser.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldNotMatchForClass(
              MARKED, "name|createdDate")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PackagePrivateField.class.getName()));
      assertFalse(failure.getMessage().contains(DateUser.class.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          namingOverload(
              "fieldsShouldNotMatchForClass(DescribedPredicate, String, Configuration)",
              naming -> naming.fieldsShouldNotMatchForClass(MARKED, "name|createdDate",
                  defaultConfiguration()),
              StaticField.class, PackagePrivateField.class));
    }
  }

  @Nested
  class InterfacesShouldNotHavePrefixI {

    @Test
    void shouldNotThrowWhenInterfaceDoesNotHavePrefixI() {
      Taikai taikai = Taikai.builder()
          .classes(ValidInterface.class)
          .java(java -> java.naming(NamingConfigurer::interfacesShouldNotHavePrefixI))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenInterfaceHasPrefixI() {
      Taikai taikai = Taikai.builder()
          .classes(IInvalidInterface.class)
          .java(java -> java.naming(NamingConfigurer::interfacesShouldNotHavePrefixI))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenInterfaceIsNamedI() {
      Taikai taikai = Taikai.builder()
          .classes(I.class)
          .java(java -> java.naming(NamingConfigurer::interfacesShouldNotHavePrefixI))
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
          namingOverload("interfacesShouldNotHavePrefixI(Configuration)",
              naming -> naming.interfacesShouldNotHavePrefixI(defaultConfiguration()),
              ValidInterface.class, IInvalidInterface.class));
    }
  }

  @Nested
  class ConstantsShouldFollowConventions {

    @Test
    void shouldNotThrowWhenConstantFollowsConventions() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithProperConstant.class)
          .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenConstantDoesNotFollowConventions() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithImproperConstant.class)
          .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldIgnoreSerialVersionUID() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithSerialVersionUID.class)
          .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldIgnoreExcludedFields() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithLoggerConstant.class)
          .java(java -> java.naming(naming -> naming.constantsShouldFollowConventions(
              List.of("log"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldIgnoreExcludedFieldsIfMultipleFieldNamesAreProvided() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithLoggerConstant.class)
          .java(java -> java.naming(naming -> naming.constantsShouldFollowConventions(
              List.of("log", "logger"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowOnSerialVersionUIDWhenNoFieldsAreExcluded() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithSerialVersionUID.class)
          .java(java -> java.naming(naming -> naming.constantsShouldFollowConventions(
              List.of())))
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
          namingOverload("constantsShouldFollowConventions(Configuration)",
              naming -> naming.constantsShouldFollowConventions(defaultConfiguration()),
              ClassWithProperConstant.class, ClassWithImproperConstant.class),
          namingOverload("constantsShouldFollowConventions(Collection, Configuration)",
              naming -> naming.constantsShouldFollowConventions(List.of("log"),
                  defaultConfiguration()),
              ClassWithLoggerConstant.class, ClassWithImproperConstant.class));
    }
  }

  @Nested
  class EnumConstantsShouldFollowConventions {

    @Test
    void shouldNotThrowWhenEnumConstantsFollowConventions() {
      Taikai taikai = Taikai.builder()
          .classes(EnumWithProperConstants.class)
          .java(java -> java.naming(NamingConfigurer::enumConstantsShouldFollowConventions))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenEnumConstantDoesNotFollowConventions() {
      Taikai taikai = Taikai.builder()
          .classes(EnumWithImproperConstants.class)
          .java(java -> java.naming(NamingConfigurer::enumConstantsShouldFollowConventions))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowForNonConstantEnumFields() {
      Taikai taikai = Taikai.builder()
          .classes(EnumWithInstanceField.class)
          .java(java -> java.naming(NamingConfigurer::enumConstantsShouldFollowConventions))
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
          namingOverload("enumConstantsShouldFollowConventions(Configuration)",
              naming -> naming.enumConstantsShouldFollowConventions(defaultConfiguration()),
              EnumWithProperConstants.class, EnumWithImproperConstants.class));
    }
  }

  @Nested
  class BooleanMethodsShouldStartWith {

    @Test
    void shouldNotThrowWhenBooleanMethodsStartWithDefaultPrefixes() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithProperBooleanMethods.class)
          .java(java -> java.naming(NamingConfigurer::booleanMethodsShouldStartWith))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenBooleanMethodDoesNotStartWithDefaultPrefixes() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithImproperBooleanMethod.class)
          .java(java -> java.naming(NamingConfigurer::booleanMethodsShouldStartWith))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenPrefixIsNotFollowedByUppercase() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithMisleadingPrefixBooleanMethod.class)
          .java(java -> java.naming(NamingConfigurer::booleanMethodsShouldStartWith))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowForEqualsOverride() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithEqualsOverride.class)
          .java(java -> java.naming(NamingConfigurer::booleanMethodsShouldStartWith))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowForRecordComponentAccessors() {
      Taikai taikai = Taikai.builder()
          .classes(RecordWithBooleanComponent.class)
          .java(java -> java.naming(NamingConfigurer::booleanMethodsShouldStartWith))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenBooleanMethodStartsWithCustomPrefix() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCustomPrefixBooleanMethod.class)
          .java(java -> java.naming(naming -> naming.booleanMethodsShouldStartWith(
              List.of("was"))))
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
          namingOverload("booleanMethodsShouldStartWith(Configuration)",
              naming -> naming.booleanMethodsShouldStartWith(defaultConfiguration()),
              ClassWithProperBooleanMethods.class, ClassWithImproperBooleanMethod.class));
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableNamingConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNamingClass.class)
          .java(java -> java.naming(naming -> {
            naming.classesShouldMatch(".*Service");
            naming.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class ClassWithProperConstant {

    static final String MY_CONSTANT = "value";
  }

  static class ClassWithImproperConstant {

    static final String myConstant = "value";
  }

  static class ClassWithSerialVersionUID implements Serializable {

    private static final long serialVersionUID = 1L;
  }

  static class ClassWithLoggerConstant {

    private static final Logger log = LoggerFactory.getLogger(ClassWithLoggerConstant.class);
  }

  enum EnumWithProperConstants {
    FIRST_VALUE, SECOND
  }

  enum EnumWithImproperConstants {
    firstValue
  }

  enum EnumWithInstanceField {
    FIRST;

    private final String label = "first";
  }

  static class ClassWithProperBooleanMethods {

    boolean isValid() {
      return true;
    }

    Boolean hasItems() {
      return true;
    }

    boolean canExecute() {
      return true;
    }

    boolean shouldRetry() {
      return true;
    }
  }

  static class ClassWithImproperBooleanMethod {

    boolean valid() {
      return true;
    }
  }

  static class ClassWithMisleadingPrefixBooleanMethod {

    boolean island() {
      return true;
    }
  }

  static class ClassWithEqualsOverride {

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }

  record RecordWithBooleanComponent(boolean active) {
  }

  static class ClassWithCustomPrefixBooleanMethod {

    boolean wasProcessed() {
      return true;
    }
  }

  @Deprecated
  static class ValidService {
  }

  static class InvalidNamingClass {
  }

  @Deprecated
  static class AnnotatedInvalidClass {
  }

  static class ClassWithCamelCaseMethod {
    void doSomething() {
    }
  }

  static class ClassWithBadMethodName {
    void Do_Something() {
    }
  }

  static class ClassWithAnnotatedMethod {
    @Deprecated
    void deprecatedMethod() {
    }
  }

  static class ClassWithAnnotatedField {
    @Deprecated
    String deprecatedField = "value";
  }

  static class ClassWithStringField {
    String myString = "value";
  }

  interface ValidInterface {
  }

  interface IInvalidInterface {
  }

  interface I {
  }

  enum FixtureStatus {
    ACTIVE
  }

  static class SnakeCaseMethods {

    void do_work() {
    }
  }
}
