package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.ArchConfiguration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class TaikaiDynamicTestsTest {

  @BeforeEach
  void setUp() {
    ArchConfiguration.get().reset();
  }

  @TestFactory
  Stream<DynamicTest> shouldRunEveryRuleAsDynamicTest() {
    return Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic())
        .build()
        .dynamicTests();
  }

  @Test
  void shouldCreateOneDynamicTestPerRuleNamedAfterDescription() {
    List<DynamicTest> tests = Taikai.builder()
        .classes(Printer.class)
        .java(java -> java
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic())
        .build()
        .dynamicTests()
        .toList();

    assertEquals(List.of(
            "Classes should not use System.out or System.err",
            "Fields should not be public unless they are static"),
        tests.stream().map(DynamicTest::getDisplayName).toList());
  }

  @Test
  void shouldPassDynamicTestOfFollowedRule() {
    DynamicTest test = Taikai.builder()
        .classes(Printer.class)
        .java(java -> java.fieldsShouldNotBePublic())
        .build()
        .dynamicTests()
        .findFirst()
        .orElseThrow();

    assertDoesNotThrow(() -> test.getExecutable().execute());
  }

  @Test
  void shouldFailDynamicTestOfViolatedRule() {
    DynamicTest test = Taikai.builder()
        .classes(Printer.class)
        .java(java -> java.noUsageOfSystemOutOrErr())
        .build()
        .dynamicTests()
        .findFirst()
        .orElseThrow();

    AssertionError error = assertThrows(AssertionError.class,
        () -> test.getExecutable().execute());

    assertTrue(error.getMessage().contains(Printer.class.getName()), error.getMessage());
  }

  @Test
  void shouldApplyExclusionsInDynamicTests() {
    DynamicTest test = Taikai.builder()
        .namespace("com.enofex.taikai")
        .excludeClasses("com.enofex.taikai.spring.ValidatedController")
        .spring(spring -> spring
            .controllers(controllers -> controllers.shouldBeAnnotatedWithController()))
        .build()
        .dynamicTests()
        .findFirst()
        .orElseThrow();

    assertDoesNotThrow(() -> test.getExecutable().execute());
  }

  @Test
  void shouldCreateNoDynamicTestsWithoutRules() {
    assertEquals(0, Taikai.builder()
        .namespace("com.enofex.taikai")
        .build()
        .dynamicTests()
        .count());
  }

  static final class Printer {

    void print() {
      System.out.println("printed");
    }
  }
}
