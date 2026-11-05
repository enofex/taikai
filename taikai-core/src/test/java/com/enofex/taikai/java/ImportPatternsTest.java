package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ImportPatternsTest {

  @ParameterizedTest
  @MethodSource("patterns")
  void shouldMatchClassesInPackage(Supplier<DescribedPredicate<JavaClass>> pattern,
      String packageIdentifier) {
    assertEquals("in package " + packageIdentifier, pattern.get().getDescription());
  }

  static Stream<Arguments> patterns() {
    return Stream.of(
        pattern(ImportPatterns::apacheCommons, "org.apache.commons.."),
        pattern(ImportPatterns::assertJ, "org.assertj.."),
        pattern(ImportPatterns::hamcrest, "org.hamcrest.."),
        pattern(ImportPatterns::hibernate, "org.hibernate.."),
        pattern(ImportPatterns::jspecify, "org.jspecify.."),
        pattern(ImportPatterns::junit, "org.junit.jupiter.."),
        pattern(ImportPatterns::logback, "ch.qos.logback.."),
        pattern(ImportPatterns::lombok, "lombok.."),
        pattern(ImportPatterns::mockito, "org.mockito.."),
        pattern(ImportPatterns::shaded, "..shaded.."),
        pattern(ImportPatterns::springBoot, "org.springframework.boot.."),
        pattern(ImportPatterns::springData, "org.springframework.data.."),
        pattern(ImportPatterns::springFramework, "org.springframework.."),
        pattern(ImportPatterns::springSecurity, "org.springframework.security.."),
        pattern(ImportPatterns::testcontainers, "org.testcontainers.."));
  }

  private static Arguments pattern(Supplier<DescribedPredicate<JavaClass>> pattern,
      String packageIdentifier) {
    return Arguments.of(pattern, packageIdentifier);
  }
}
