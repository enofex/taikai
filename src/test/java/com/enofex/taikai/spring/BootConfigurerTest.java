package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class BootConfigurerTest {

  @Nested
  class ApplicationClassShouldResideInPackage {

    @Test
    void shouldNotThrowWhenApplicationClassInCorrectPackage() {
      Taikai taikai = Taikai.builder()
          .classes(CorrectPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.applicationClassShouldResideInPackage("com.enofex.taikai.spring")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenApplicationClassOutsideConfiguredPackage() {
      Taikai taikai = Taikai.builder()
          .classes(WrongPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.applicationClassShouldResideInPackage("com.example.other")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @SpringBootApplication
  static class CorrectPackageApplication {

  }

  @SpringBootApplication
  static class WrongPackageApplication {

  }
}
