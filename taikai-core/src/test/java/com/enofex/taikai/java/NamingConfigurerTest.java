package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NamingConfigurerTest {

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
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ".*Service",
              defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
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
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCamelCaseMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch(
              "[a-z][a-zA-Z0-9]*",
              defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
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
  }

  @Nested
  class ClassesShouldMatchByAnnotation {

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
  }

  @Nested
  class MethodsShouldMatchByAnnotation {

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
  }

  @Nested
  class FieldsShouldMatchByAnnotation {

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
    void shouldThrowWhenFieldNameDoesNotMatchPatternWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch("other.*",
              defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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
  }

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
    void shouldSupportConfigurationOverload() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithProperConstant.class)
          .java(java -> java.naming(naming -> naming.constantsShouldFollowConventions(
              defaultConfiguration())))
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

    @Test
    void shouldSupportConfigurationOverload() {
      Taikai taikai = Taikai.builder()
          .classes(EnumWithProperConstants.class)
          .java(java -> java.naming(naming -> naming.enumConstantsShouldFollowConventions(
              defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
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

    @Test
    void shouldSupportConfigurationOverload() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithProperBooleanMethods.class)
          .java(java -> java.naming(naming -> naming.booleanMethodsShouldStartWith(
              defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ConfigurationOverloads {

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithConfiguration(Customizer<NamingConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      Taikai compliantTaikai = Taikai.builder()
          .classes(compliant)
          .java(java -> java.naming(rule))
          .build();
      Taikai violatingTaikai = Taikai.builder()
          .classes(violating)
          .java(java -> java.naming(rule))
          .build();

      assertDoesNotThrow(compliantTaikai::check);
      assertThrows(AssertionError.class, violatingTaikai::check);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.classesShouldMatch(ClassCriteria.annotatedWith(Deprecated.class),
                  ".*Service", defaultConfiguration()),
              ValidService.class, AnnotatedInvalidClass.class),
          overload("methodsShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.methodsShouldMatch(MethodCriteria.nameMatching(".*Something"),
                  "[a-z][a-zA-Z0-9]*", defaultConfiguration()),
              ClassWithCamelCaseMethod.class, ClassWithBadMethodName.class),
          overload("fieldsShouldMatch(DescribedPredicate, String, Configuration)",
              naming -> naming.fieldsShouldMatch(FieldCriteria.ofType(String.class), "my.*",
                  defaultConfiguration()),
              ClassWithStringField.class, ClassWithAnnotatedField.class));
    }

    private static Arguments overload(String name, Customizer<NamingConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      return Arguments.of(Named.of(name, rule), compliant, violating);
    }
  }

  static class ClassWithProperConstant {

    static final String MY_CONSTANT = "value";
  }

  static class ClassWithImproperConstant {

    static final String myConstant = "value";
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
}
