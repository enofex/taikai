package com.enofex.taikai.java;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.importsOverload;
import static com.enofex.taikai.java.JavaFixtures.MARKED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.Namespace;
import com.enofex.taikai.core.TaikaiException;
import com.enofex.taikai.core.TaikaiRule.Configuration;
import com.enofex.taikai.java.JavaFixtures.DateUser;
import com.enofex.taikai.java.JavaFixtures.PlainClass;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ImportsConfigurerTest {

  @Nested
  class ShouldNotImport {

    @Test
    void shouldNotImportSpecificPackage() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingDisallowedImport.class)
          .java(java -> java.imports(imports -> imports.shouldNotImport(
              ClassCriteria.inPackage("java.util"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotImportByName() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingDisallowedImport.class)
          .java(java -> java.imports(imports -> imports.shouldNotImport(
              ClassCriteria.nameMatching(".*ClassUsingDisallowedImport"),
              ClassCriteria.nameMatching("java\\.util\\..*"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowValidImports() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          .java(java -> java.imports(imports -> imports.shouldNotImport(
              ClassCriteria.inPackage("java.sql"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenUnselectedClassesImportForbiddenTypes() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.imports(imports -> imports
              .shouldNotImport(MARKED, ClassCriteria.inPackage("java.util"))))
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
          importsOverload("shouldNotImport(DescribedPredicate, Configuration)",
              imports -> imports.shouldNotImport(ClassCriteria.inPackage("java.util"),
                  defaultConfiguration()),
              ClassUsingAllowedImport.class, ClassUsingDisallowedImport.class),
          importsOverload("shouldNotImport(DescribedPredicate, DescribedPredicate, Configuration)",
              imports -> imports.shouldNotImport(
                  ClassCriteria.simpleNameMatching("ClassUsing.*Import"),
                  ClassCriteria.inPackage("java.util"), defaultConfiguration()),
              ClassUsingAllowedImport.class, ClassUsingDisallowedImport.class));
    }
  }

  @Nested
  class ShouldImport {

    @Test
    void shouldImportByName() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          .java(java -> java.imports(imports -> imports.shouldImport(
              ClassCriteria.nameMatching(".*ClassUsingAllowedImport"),
              ClassCriteria.nameMatching("java\\.lang\\..*"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowExceptionForImportByName() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          .java(java -> java.imports(imports -> imports.shouldImport(
              ClassCriteria.nameMatching(".*ClassUsingAllowedImport"),
              ClassCriteria.nameMatching("java\\.not\\.found..*"))))
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
          importsOverload("shouldImport(DescribedPredicate, DescribedPredicate, Configuration)",
              imports -> imports.shouldImport(
                  ClassCriteria.simpleNameMatching("DateUser|PlainClass"),
                  ClassCriteria.inPackage("java.util"), defaultConfiguration()),
              DateUser.class, PlainClass.class));
    }
  }

  @Nested
  class ShouldHaveNoCycles {

    @Test
    void shouldThrowWhenNamespaceNotSetForCycles() {
      assertThrows(TaikaiException.class, () -> Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          // no namespace configured
          .java(java -> java.imports(ImportsConfigurer::shouldHaveNoCycles))
          .build());
    }

    @Test
    void shouldThrowWhenNamespaceNotSetForCyclesWithConfiguration() {
      assertThrows(TaikaiException.class, () -> Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          .java(java -> java.imports(
              imports -> imports.shouldHaveNoCycles(defaultConfiguration())))
          .build());
    }

    @Test
    void shouldNotThrowWhenNamespaceSetInConfigurationForCycles() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingAllowedImport.class)
          .java(java -> java.imports(
              imports -> imports.shouldHaveNoCycles(Configuration.of(
                  "com.enofex.taikai", Namespace.Import.WITHOUT_TESTS))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableImportsConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ClassUsingDisallowedImport.class)
          .java(java -> java.imports(imports -> {
            imports.shouldNotImport(ClassCriteria.inPackage("java.util"));
            imports.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class ClassUsingAllowedImport {

    private final java.lang.String value = "ok";
  }

  static class ClassUsingDisallowedImport {

    private final java.util.List<String> list = java.util.Collections.emptyList();
  }
}
