package com.enofex.taikai.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class TestConfigurerTest {

  @Test
  void shouldApplyJunitCustomizer() {
    Taikai taikai = Taikai.builder()
        .classes(ValidTestClass.class)
        .test(test -> test.junit(
            junit -> junit.methodsShouldBePackagePrivate()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  @SuppressWarnings("deprecation")
  void shouldApplyJunit5DeprecatedCustomizer() {
    Taikai taikai = Taikai.builder()
        .classes(ValidTestClass.class)
        .test(test -> test.junit5(
            junit -> junit.methodsShouldBePackagePrivate()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldDisableTestConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(ValidTestClass.class)
        .test(test -> {
          test.junit(junit -> junit.methodsShouldBePackagePrivate());
          test.disable();
        })
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class ValidTestClass {

    @org.junit.jupiter.api.Test
    void shouldWork() {
    }
  }
}
