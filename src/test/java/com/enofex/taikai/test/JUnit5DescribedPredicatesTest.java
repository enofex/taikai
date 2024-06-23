package com.enofex.taikai.test;

import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_PARAMETRIZED_TEST;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.annotatedWithTestOrParameterizedTest;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.conditions.ArchConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;

class JUnit5DescribedPredicatesTest {

  @Test
  void shouldIdentifyClassesAnnotatedWithTestOrParameterizedTest() {
    JavaClasses importedClasses = new ClassFileImporter().importClasses(
        TestExample.class, ParameterizedTestExample.class);

    ArchRule rule = methods().that(annotatedWithTestOrParameterizedTest(false))
        .should(beAnnotatedWith(ANNOTATION_TEST)
            .or(beAnnotatedWith(ANNOTATION_PARAMETRIZED_TEST)));

    rule.check(importedClasses);
  }

  @Test
  void shouldIdentifyClassesMetaAnnotatedWithTestOrParameterizedTest() {
    JavaClasses importedClasses = new ClassFileImporter().importClasses(
        MetaTestExample.class, MetaParameterizedTestExample.class);

    ArchRule rule = methods().that(annotatedWithTestOrParameterizedTest(true))
        .should(ArchConditions.beMetaAnnotatedWith(ANNOTATION_TEST)
            .or(ArchConditions.beMetaAnnotatedWith(ANNOTATION_PARAMETRIZED_TEST)));

    rule.check(importedClasses);
  }

  private static final class TestExample {

    @Test
    void testMethod() {
    }
  }

  private static final class ParameterizedTestExample {

    @ParameterizedTest
    @EmptySource
    void parameterizedTestMethod(String empty) {
    }
  }

  private static class MetaTestExample {

    @TestAnnotation
    void metaTestMethod() {
    }
  }

  private static final class MetaParameterizedTestExample {

    @ParameterizedTestAnnotation
    @EmptySource
    void metaParameterizedTestMethod(String empty) {
    }
  }

  @Test
  private @interface TestAnnotation {
  }

  @ParameterizedTest
  private @interface ParameterizedTestAnnotation {
  }
}
