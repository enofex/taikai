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
            JUnitConfigurer::methodsShouldBePackagePrivate))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldDisableTestConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(ValidTestClass.class)
        .test(test -> {
          test.junit(JUnitConfigurer::methodsShouldBePackagePrivate);
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
