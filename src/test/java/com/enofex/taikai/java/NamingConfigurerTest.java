package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
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
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
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
  class ClassesAnnotatedWithShouldMatch {

    @Test
    void shouldNotThrowWhenAnnotatedClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesAnnotatedWithShouldMatch(
              Deprecated.class, ".*Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedInvalidClass.class)
          .java(java -> java.naming(naming -> naming.classesAnnotatedWithShouldMatch(
              Deprecated.class, ".*Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesImplementingShouldMatch {

    @Test
    void shouldNotThrowWhenImplementingClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesImplementingShouldMatch(
              java.io.Serializable.class, ".*Serializable")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenImplementingClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidSerialImpl.class)
          .java(java -> java.naming(naming -> naming.classesImplementingShouldMatch(
              java.io.Serializable.class, ".*Serializable")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesAssignableToShouldMatch {

    @Test
    void shouldNotThrowWhenAssignableClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesAssignableToShouldMatch(
              java.io.Serializable.class, ".*Serializable")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAssignableClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidSerialImpl.class)
          .java(java -> java.naming(naming -> naming.classesAssignableToShouldMatch(
              java.io.Serializable.class, ".*Serializable")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldMatch {

    @Test
    void shouldNotThrowWhenAnnotatedMethodNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsAnnotatedWithShouldMatch(
              Deprecated.class, "deprecated.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedMethodNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsAnnotatedWithShouldMatch(
              Deprecated.class, "valid.*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class FieldsAnnotatedWithShouldMatch {

    @Test
    void shouldNotThrowWhenAnnotatedFieldNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsAnnotatedWithShouldMatch(
              Deprecated.class, "deprecatedField")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsAnnotatedWithShouldMatch(
              Deprecated.class, "validField")))
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
              String.class, "my.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldNameOfTypeDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              String.class, "other.*")))
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
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
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
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
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
  class ClassesAnnotatedWithShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesAnnotatedWithShouldMatch(
              Deprecated.class.getName(), ".*Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesAnnotatedWithShouldMatch(
              Deprecated.class, ".*Service",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesImplementingShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesImplementingShouldMatch(
              java.io.Serializable.class.getName(), ".*Serializable")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassType() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesImplementingShouldMatch(
              java.io.Serializable.class, ".*Serializable",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesAssignableToShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesAssignableToShouldMatch(
              java.io.Serializable.class.getName(), ".*Serializable")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassType() {
      Taikai taikai = Taikai.builder()
          .classes(ValidSerializable.class)
          .java(java -> java.naming(naming -> naming.classesAssignableToShouldMatch(
              java.io.Serializable.class, ".*Serializable",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsAnnotatedWithShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsAnnotatedWithShouldMatch(
              Deprecated.class.getName(), "deprecated.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedMethod.class)
          .java(java -> java.naming(naming -> naming.methodsAnnotatedWithShouldMatch(
              Deprecated.class, "deprecated.*",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsAnnotatedWithShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsAnnotatedWithShouldMatch(
              Deprecated.class.getName(), "deprecatedField")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassAnnotationType() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAnnotatedField.class)
          .java(java -> java.naming(naming -> naming.fieldsAnnotatedWithShouldMatch(
              Deprecated.class, "deprecatedField",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldMatchStringOverloads {

    @Test
    void shouldNotThrowUsingStringTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              String.class.getName(), "my.*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationWithClassType() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStringField.class)
          .java(java -> java.naming(naming -> naming.fieldsShouldMatch(
              String.class, "my.*",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
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

  static class ValidSerializable implements java.io.Serializable {
  }

  static class InvalidSerialImpl implements java.io.Serializable {
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
}
