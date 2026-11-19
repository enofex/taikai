package com.enofex.taikai.java;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.imported;
import static com.enofex.taikai.java.JavaChecks.overload;
import static com.enofex.taikai.java.JavaFixtures.THIS_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.MethodCriteria;
import com.enofex.taikai.core.TaikaiRule.Configuration;
import com.enofex.taikai.java.JavaFixtures.Entity;
import com.enofex.taikai.java.JavaFixtures.HttpStatusException;
import com.enofex.taikai.java.JavaFixtures.MarkedClass;
import com.enofex.taikai.java.JavaFixtures.Marker;
import com.enofex.taikai.java.JavaFixtures.QueryMethods;
import com.enofex.taikai.java.JavaFixtures.Required;
import com.enofex.taikai.java.JavaFixtures.TestAnnotation;
import com.enofex.taikai.java.JavaFixtures.ThrowsHttpException;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.ResponseEntity;

class JavaConfigurerMethodsTest {

  private static final DescribedPredicate<JavaMethod> FIRST = MethodCriteria.nameMatching("first");
  private static final DescribedPredicate<JavaClass> IN_THIS_PACKAGE =
      ClassCriteria.inPackage(THIS_PACKAGE);

  @Test
  void shouldCombineRequiredAndForbiddenReturnTypes() {
    Taikai taikai = Taikai.builder()
        .classes(DtoReturn.class)
        .java(java -> java
            .methodsShouldReturnForClass(ClassCriteria.simpleNameMatching("DtoReturn"),
                ClassCriteria.nameMatching(".*DTO"))
            .methodsShouldNotReturnForClass(ClassCriteria.simpleNameMatching("DtoReturn"),
                ClassCriteria.annotatedWith(Entity.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(UserEntity.class.getName()));
  }

  @Nested
  class MethodsShouldNotReturn {

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("methodsShouldNotReturn(DescribedPredicate, DescribedPredicate)",
              java -> java.methodsShouldNotReturn(MethodCriteria.nameMatching("result"),
                  ClassCriteria.annotatedWith(Entity.class)),
              SafeReturn.class, DirectReturn.class),
          overload("methodsShouldNotReturn(DescribedPredicate, DescribedPredicate, Configuration)",
              java -> java.methodsShouldNotReturn(MethodCriteria.nameMatching("result"),
                  ClassCriteria.annotatedWith(Entity.class), defaultConfiguration()),
              SafeReturn.class, DirectReturn.class));
    }
  }

  @Nested
  class MethodsShouldNotReturnForClass {

    @ParameterizedTest
    @ArgumentsSource(EntitySignatures.class)
    void shouldRejectEntitiesAnywhereInReturnSignature(Class<?> fixture) {
      Taikai taikai = Taikai.builder()
          .classes(fixture)
          .java(java -> java.methodsShouldNotReturnForClass(IN_THIS_PACKAGE,
              ClassCriteria.annotatedWith(Entity.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("result"));
      assertTrue(failure.getMessage().contains(UserEntity.class.getName()));
    }

    @Test
    void shouldRejectTypesSelectedByNameOrPackage() {
      Taikai nameRule = Taikai.builder()
          .classes(DirectReturn.class)
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.simpleNameMatching("DirectReturn"),
              ClassCriteria.nameMatching(".*Entity")))
          .build();

      assertThrows(AssertionError.class, nameRule::check);

      Taikai packageRule = Taikai.builder()
          .classes(NestedReturn.class)
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.simpleNameMatching("NestedReturn"),
              ClassCriteria.inPackage("org.springframework.http")))
          .build();

      assertThrows(AssertionError.class, packageRule::check);
    }

    @Test
    void shouldAllowSafeReturnsAndUnselectedClasses() {
      Taikai taikai = Taikai.builder()
          .classes(SafeReturn.class, DirectReturn.class)
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.simpleNameMatching("SafeReturn"),
              ClassCriteria.annotatedWith(Entity.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldAllowForbiddenTypesOutsideTheReturnSignature() {
      Taikai taikai = Taikai.builder()
          .classes(UsesEntity.class)
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.simpleNameMatching("UsesEntity"),
              ClassCriteria.annotatedWith(Entity.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldHonorConfigurationExclusions() {
      Taikai taikai = Taikai.builder()
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.simpleNameMatching("DirectReturn|SafeReturn"),
              ClassCriteria.annotatedWith(Entity.class),
              Configuration.of(imported(DirectReturn.class, SafeReturn.class),
                  List.of(DirectReturn.class.getName()))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldAllowNoMatchingClasses() {
      Taikai taikai = Taikai.builder()
          .classes(DirectReturn.class)
          .java(java -> java.methodsShouldNotReturnForClass(
              ClassCriteria.inPackage("com.company.controllers.."),
              ClassCriteria.annotatedWith(Entity.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldReturn {

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("methodsShouldReturn(DescribedPredicate, DescribedPredicate)",
              java -> java.methodsShouldReturn(MethodCriteria.nameMatching("result"),
                  ClassCriteria.annotatedWith(Entity.class)),
              DirectReturn.class, SafeReturn.class),
          overload("methodsShouldReturn(DescribedPredicate, DescribedPredicate, Configuration)",
              java -> java.methodsShouldReturn(MethodCriteria.nameMatching("result"),
                  ClassCriteria.annotatedWith(Entity.class), defaultConfiguration()),
              DirectReturn.class, SafeReturn.class));
    }
  }

  @Nested
  class MethodsShouldReturnForClass {

    @ParameterizedTest
    @ArgumentsSource(EntitySignatures.class)
    void shouldAcceptRequiredTypesAnywhereInReturnSignature(Class<?> fixture) {
      Taikai taikai = Taikai.builder()
          .classes(fixture)
          .java(java -> java.methodsShouldReturnForClass(IN_THIS_PACKAGE,
              ClassCriteria.annotatedWith(Entity.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldRejectEachMethodWithoutRequiredReturnType() {
      Taikai taikai = Taikai.builder()
          .classes(SafeReturn.class)
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.simpleNameMatching("SafeReturn"),
              ClassCriteria.annotatedWith(Entity.class)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("result()"));
      assertTrue(failure.getMessage().contains("nothing()"));
      assertTrue(failure.getMessage().contains("count()"));
    }

    @Test
    void shouldRequireClassNamesEndingInDtoIncludingWrappedReturns() {
      Taikai taikai = Taikai.builder()
          .classes(DtoReturn.class)
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.simpleNameMatching("DtoReturn"), ClassCriteria.nameMatching(".*DTO")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldAcceptRequiredTypesSelectedByPackage() {
      Taikai taikai = Taikai.builder()
          .classes(NestedReturn.class)
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.simpleNameMatching("NestedReturn"),
              ClassCriteria.inPackage("org.springframework.http")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldIgnoreUnselectedClassesForRequiredReturns() {
      Taikai taikai = Taikai.builder()
          .classes(DirectReturn.class, SafeReturn.class)
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.simpleNameMatching("DirectReturn"),
              ClassCriteria.annotatedWith(Entity.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldHonorConfigurationExclusionsForRequiredReturns() {
      Taikai taikai = Taikai.builder()
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.simpleNameMatching("DirectReturn|SafeReturn"),
              ClassCriteria.annotatedWith(Entity.class),
              Configuration.of(imported(DirectReturn.class, SafeReturn.class),
                  List.of(SafeReturn.class.getName()))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldAllowNoMatchingClassesForRequiredReturns() {
      Taikai taikai = Taikai.builder()
          .classes(SafeReturn.class)
          .java(java -> java.methodsShouldReturnForClass(
              ClassCriteria.inPackage("com.company.controllers.."),
              ClassCriteria.nameMatching(".*DTO")))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotDeclareGenericExceptions {

    @Test
    void shouldNotThrowWhenNoMethodsDeclareGenericExceptions() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresNoExceptions.class)
          .java(JavaConfigurer::methodsShouldNotDeclareGenericExceptions)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsDeclareExceptionOrRuntimeException() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresGenericExceptions.class)
          .java(JavaConfigurer::methodsShouldNotDeclareGenericExceptions)
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
          overload("methodsShouldNotDeclareGenericExceptions(Configuration)",
              java -> java.methodsShouldNotDeclareGenericExceptions(defaultConfiguration()),
              DeclaresNoExceptions.class, DeclaresGenericExceptions.class));
    }
  }

  @Nested
  class MethodsShouldNotDeclareException {

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresIOException.class)
          .java(java -> java.methodsShouldNotDeclareException(
              MethodCriteria.nameMatching("nonExistingMethod"), IOException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

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

    @Test
    void shouldAllowUncheckedThrowWithoutThrowsClauseInDeclarationRule() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
              HttpStatusException.class))
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
          overload("methodsShouldNotDeclareException(DescribedPredicate, Class)",
              java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
                  IOException.class),
              DeclaresNoExceptions.class, DeclaresIOException.class),
          overload("methodsShouldNotDeclareException(DescribedPredicate, Class, Configuration)",
              java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
                  IOException.class, defaultConfiguration()),
              MarkedClass.class, ThrowingMethods.class),
          overload("methodsShouldNotDeclareException(DescribedPredicate, String)",
              java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
                  IOException.class.getName()),
              DeclaresNoExceptions.class, DeclaresIOException.class),
          overload("methodsShouldNotDeclareException(DescribedPredicate, String, Configuration)",
              java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
                  IOException.class.getName(), defaultConfiguration()),
              DeclaresNoExceptions.class, DeclaresIOException.class));
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
              ClassCriteria.simpleNameMatching("ThrowingMethods"),
              ClassCriteria.inPackage("java.sql")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoMatchingExceptionIsDeclared() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowingMethods.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("ThrowingMethods"),
              ClassCriteria.inPackage("java.net")))
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
          .anyMatch(method -> method.getModifiers().contains(JavaModifier.SYNTHETIC)
              && method.getThrowsClause().containsType(Exception.class)));

      Taikai taikai = Taikai.builder()
          .classes(CallableTask.class)
          .java(java -> java.methodsShouldNotDeclareExceptionForClass(
              ClassCriteria.simpleNameMatching("CallableTask"), Exception.class))
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
          overload("methodsShouldNotDeclareExceptionForClass(DescribedPredicate, Class, "
                  + "Configuration)",
              java -> java.methodsShouldNotDeclareExceptionForClass(IN_THIS_PACKAGE,
                  IOException.class, defaultConfiguration()),
              DeclaresNoExceptions.class, DeclaresIOException.class),
          overload("methodsShouldNotDeclareExceptionForClass(DescribedPredicate, String)",
              java -> java.methodsShouldNotDeclareExceptionForClass(IN_THIS_PACKAGE,
                  IOException.class.getName()),
              DeclaresNoExceptions.class, DeclaresIOException.class),
          overload("methodsShouldNotDeclareExceptionForClass(DescribedPredicate, String, "
                  + "Configuration)",
              java -> java.methodsShouldNotDeclareExceptionForClass(IN_THIS_PACKAGE,
                  IOException.class.getName(), defaultConfiguration()),
              DeclaresNoExceptions.class, DeclaresIOException.class),
          overload("methodsShouldNotDeclareExceptionForClass(DescribedPredicate, "
                  + "DescribedPredicate, Configuration)",
              java -> java.methodsShouldNotDeclareExceptionForClass(IN_THIS_PACKAGE,
                  ClassCriteria.type(IOException.class), defaultConfiguration()),
              DeclaresNoExceptions.class, DeclaresIOException.class));
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(
              MethodCriteria.nameMatching("nonExistent"), ProcessAnnotation.class))
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
          overload("methodsShouldBeAnnotatedWith(DescribedPredicate, Class)",
              java -> java.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("process"),
                  ProcessAnnotation.class),
              AnnotatedService.class, NonAnnotatedService.class),
          overload("methodsShouldBeAnnotatedWith(DescribedPredicate, Class, Configuration)",
              java -> java.methodsShouldBeAnnotatedWith(FIRST, Required.class,
                  defaultConfiguration()),
              AnnotatedMethods.class, PublicUnannotatedMethods.class),
          overload("methodsShouldBeAnnotatedWith(DescribedPredicate, String)",
              java -> java.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("process"),
                  ProcessAnnotation.class.getName()),
              AnnotatedService.class, NonAnnotatedService.class),
          overload("methodsShouldBeAnnotatedWith(DescribedPredicate, String, Configuration)",
              java -> java.methodsShouldBeAnnotatedWith(FIRST, Required.class.getName(),
                  defaultConfiguration()),
              AnnotatedMethods.class, PublicUnannotatedMethods.class));
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("methodsShouldBeAnnotatedWithForClass(DescribedPredicate, Class, "
                  + "Configuration)",
              java -> java.methodsShouldBeAnnotatedWithForClass(IN_THIS_PACKAGE,
                  Required.class, defaultConfiguration()),
              AnnotatedMethods.class, PartlyAnnotatedMethods.class),
          overload("methodsShouldBeAnnotatedWithForClass(DescribedPredicate, String)",
              java -> java.methodsShouldBeAnnotatedWithForClass(IN_THIS_PACKAGE,
                  Required.class.getName()),
              AnnotatedMethods.class, PartlyAnnotatedMethods.class),
          overload("methodsShouldBeAnnotatedWithForClass(DescribedPredicate, String, "
                  + "Configuration)",
              java -> java.methodsShouldBeAnnotatedWithForClass(IN_THIS_PACKAGE,
                  Required.class.getName(), defaultConfiguration()),
              AnnotatedMethods.class, PartlyAnnotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWithAll {

    @Test
    void shouldNotThrowWhenMethodHasAllRequiredAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(FullyAnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(BaseAnnotation.class),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsMissingOneRequiredAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(PartiallyAnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(BaseAnnotation.class),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodIsNotAnnotatedWithBaseAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(NonAnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              MethodCriteria.annotatedWith(BaseAnnotation.class),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
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
          overload("methodsShouldBeAnnotatedWithAll(DescribedPredicate, Collection, Configuration)",
              java -> java.methodsShouldBeAnnotatedWithAll(FIRST,
                  List.of(Required.class.getName()), defaultConfiguration()),
              AnnotatedMethods.class, PublicUnannotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldBeAnnotatedWithAllForClass {

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("methodsShouldBeAnnotatedWithAllForClass(DescribedPredicate, Collection)",
              java -> java.methodsShouldBeAnnotatedWithAllForClass(IN_THIS_PACKAGE,
                  List.of(Required.class.getName())),
              AnnotatedMethods.class, PartlyAnnotatedMethods.class),
          overload("methodsShouldBeAnnotatedWithAllForClass(DescribedPredicate, Collection, "
                  + "Configuration)",
              java -> java.methodsShouldBeAnnotatedWithAllForClass(IN_THIS_PACKAGE,
                  List.of(Required.class.getName()), defaultConfiguration()),
              AnnotatedMethods.class, PartlyAnnotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldNotBeAnnotatedWith {

    @Test
    void shouldNotThrowWhenMethodHasOnlyConflictingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(OnlyConflictingService.class)
          .java(java -> java.methodsShouldNotBeAnnotatedWith(
              MethodCriteria.annotatedWith(PrimaryAnnotation.class), ConflictingAnnotation.class))
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
          overload("methodsShouldNotBeAnnotatedWith(DescribedPredicate, Class)",
              java -> java.methodsShouldNotBeAnnotatedWith(
                  MethodCriteria.annotatedWith(PrimaryAnnotation.class),
                  ConflictingAnnotation.class),
              SafeAnnotatedService.class, ConflictingAnnotationsService.class),
          overload("methodsShouldNotBeAnnotatedWith(DescribedPredicate, Class, Configuration)",
              java -> java.methodsShouldNotBeAnnotatedWith(FIRST, Required.class,
                  defaultConfiguration()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class),
          overload("methodsShouldNotBeAnnotatedWith(DescribedPredicate, String)",
              java -> java.methodsShouldNotBeAnnotatedWith(
                  MethodCriteria.annotatedWith(PrimaryAnnotation.class.getName()),
                  ConflictingAnnotation.class.getName()),
              SafeAnnotatedService.class, ConflictingAnnotationsService.class),
          overload("methodsShouldNotBeAnnotatedWith(DescribedPredicate, String, Configuration)",
              java -> java.methodsShouldNotBeAnnotatedWith(FIRST, Required.class.getName(),
                  defaultConfiguration()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldNotBeAnnotatedWithForClass {

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, Class)",
              java -> java.methodsShouldNotBeAnnotatedWithForClass(IN_THIS_PACKAGE, Required.class),
              PublicUnannotatedMethods.class, AnnotatedMethods.class),
          overload("methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, Class, "
                  + "Configuration)",
              java -> java.methodsShouldNotBeAnnotatedWithForClass(IN_THIS_PACKAGE, Required.class,
                  defaultConfiguration()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class),
          overload("methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, String)",
              java -> java.methodsShouldNotBeAnnotatedWithForClass(IN_THIS_PACKAGE,
                  Required.class.getName()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class),
          overload("methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, String, "
                  + "Configuration)",
              java -> java.methodsShouldNotBeAnnotatedWithForClass(IN_THIS_PACKAGE,
                  Required.class.getName(), defaultConfiguration()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsMatchRegexAndHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldHaveModifiers(MethodCriteria.nameMatching(".*"),
              List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsMatchRegexButDoNotHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MixedModifiers.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.nameMatching("one|two|three"), List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(TestAnnotation.class), Set.of()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldCheckOnlyMethodsMatchingAllCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(QueryMethods.class)
          .java(java -> java.methodsShouldHaveModifiers(
              MethodCriteria.annotatedWith(Marker.class)
                  .and(MethodCriteria.nameMatching("find.*")),
              List.of(JavaModifier.PUBLIC)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("findHidden"));
      assertFalse(failure.getMessage().contains("save"));
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
          overload("methodsShouldHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.methodsShouldHaveModifiers(FIRST, List.of(JavaModifier.PUBLIC),
                  defaultConfiguration()),
              PublicUnannotatedMethods.class, AnnotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenAllMethodsInClassHaveRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(
              ClassCriteria.nameMatching(".*PublicMethods"), List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassLacksRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(MixedModifiers.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(
              ClassCriteria.nameMatching(".*MixedModifiers"), List.of(JavaModifier.PRIVATE)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldIgnoreUnselectedClasses() {
      Taikai taikai = Taikai.builder()
          .classes(PublicUnannotatedMethods.class, MarkedClass.class)
          .java(java -> java.methodsShouldHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PublicUnannotatedMethods"),
              List.of(JavaModifier.PUBLIC)))
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
          overload("methodsShouldHaveModifiersForClass(DescribedPredicate, Collection, "
                  + "Configuration)",
              java -> java.methodsShouldHaveModifiersForClass(IN_THIS_PACKAGE,
                  List.of(JavaModifier.PUBLIC), defaultConfiguration()),
              PublicUnannotatedMethods.class, MarkedClass.class));
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenMethodsDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(MethodCriteria.nameMatching(".*"),
              List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(
              MethodCriteria.nameMatching(".*anotherPublicMethod"), List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(
              MethodCriteria.nameMatching("noSuchMethod"), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiers(MethodCriteria.nameMatching(".*"),
              Set.of()))
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
          overload("methodsShouldNotHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.methodsShouldNotHaveModifiers(FIRST, List.of(JavaModifier.PUBLIC),
                  defaultConfiguration()),
              AnnotatedMethods.class, PublicUnannotatedMethods.class));
    }
  }

  @Nested
  class MethodsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenAllMethodsInClassDoNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.nameMatching(".*PublicMethods"), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnyMethodInClassHasForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.nameMatching(".*StaticMethods"), List.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoClassMatches() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.nameMatching(".*NonExistentClass"), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(StaticMethods.class)
          .java(java -> java.methodsShouldNotHaveModifiersForClass(
              ClassCriteria.nameMatching(".*StaticMethods"), Set.of()))
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
          overload("methodsShouldNotHaveModifiersForClass(DescribedPredicate, Collection, "
                  + "Configuration)",
              java -> java.methodsShouldNotHaveModifiersForClass(IN_THIS_PACKAGE,
                  List.of(JavaModifier.PUBLIC), defaultConfiguration()),
              MarkedClass.class, PublicUnannotatedMethods.class));
    }
  }

  static class EntitySignatures implements ArgumentsProvider {

    @Override
    public Stream<Arguments> provideArguments(ParameterDeclarations parameters,
        ExtensionContext context) {
      return Stream.of(DirectReturn.class, ArrayReturn.class, NestedReturn.class,
              UpperWildcardReturn.class, LowerWildcardReturn.class, TypeVariableReturn.class,
              GenericArrayReturn.class, RecursiveBoundReturn.class)
          .map(Arguments::of);
    }
  }

  @Entity
  static class UserEntity {
  }

  static class UserDTO {
  }

  static class DtoReturn {
    UserDTO result() {
      return null;
    }

    ResponseEntity<List<UserDTO>> wrapped() {
      return null;
    }

    Map<UserEntity, UserDTO> mixed() {
      return null;
    }
  }

  static class DirectReturn {
    // ArchUnit inspects this method's bytecode to verify private return signatures.
    @SuppressWarnings("java:S1144")
    private UserEntity result() {
      return null;
    }
  }

  static class ArrayReturn {
    UserEntity[][] result() {
      return null;
    }
  }

  static class NestedReturn {
    ResponseEntity<Map<String, List<UserEntity>>> result() {
      return null;
    }
  }

  static class UpperWildcardReturn {
    List<? extends UserEntity> result() {
      return null;
    }
  }

  static class LowerWildcardReturn {
    List<? super UserEntity> result() {
      return null;
    }
  }

  static class TypeVariableReturn<T extends UserEntity> {
    T result() {
      return null;
    }
  }

  static class GenericArrayReturn {
    <T extends UserEntity> T[] result() {
      return null;
    }
  }

  static class RecursiveBoundReturn {
    <T extends UserEntity & Comparable<T>> T result() {
      return null;
    }
  }

  static class SafeReturn {
    List<String> result() {
      return null;
    }

    void nothing() {
      // This fixture verifies that void return types are allowed.
    }

    int count() {
      return 0;
    }
  }

  static class UsesEntity {
    private UserEntity entity;

    String result(UserEntity argument) {
      this.entity = argument;
      return "safe";
    }
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

  static class DeclaresNoExceptions {

    void safeMethod() {
    }

    void anotherSafeMethod() {
    }
  }

  static class DeclaresGenericExceptions {

    void methodThrowsException() throws Exception {
      throw new Exception("generic exception");
    }

    void methodThrowsRuntimeException() throws RuntimeException {
      throw new RuntimeException("runtime");
    }
  }

  static class DeclaresIOException {

    void riskyOperation() throws IOException {
      throw new IOException("I/O error");
    }

    void anotherRiskyOperation() throws IOException {
      throw new IOException("Another I/O error");
    }
  }

  static class ThrowingMethods {

    void load() throws IOException {
    }

    void query() throws SQLException {
    }
  }

  static class CallableTask implements Callable<String> {

    @Override
    public String call() throws IOException {
      return "done";
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ProcessAnnotation {
  }

  static class AnnotatedService {
    @ProcessAnnotation
    void process() {
      // annotated correctly
    }

    void otherMethod() {
      // ignored, name does not match
    }
  }

  static class NonAnnotatedService {
    void process() {
      // carries no annotation
    }
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

  static class PublicUnannotatedMethods {

    public void first() {
    }

    public void second() {
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface BaseAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface RequiredA {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface RequiredB {
  }

  static class FullyAnnotatedService {

    @BaseAnnotation
    @RequiredA
    @RequiredB
    void process() {
    }
  }

  static class PartiallyAnnotatedService {

    @BaseAnnotation
    @RequiredA
    void process() {
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface PrimaryAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ConflictingAnnotation {
  }

  static class ConflictingAnnotationsService {

    @PrimaryAnnotation
    @ConflictingAnnotation
    void doSomething() {
    }
  }

  static class SafeAnnotatedService {

    @PrimaryAnnotation
    void doSomething() {
    }
  }

  static class OnlyConflictingService {

    @ConflictingAnnotation
    void doSomething() {
    }
  }

  static class PublicMethods {

    @TestAnnotation
    public void publicMethod() {}

    public void anotherPublicMethod() {}
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
