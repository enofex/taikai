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

  @Nested
  class SpringBootApplicationShouldBeInDeprecated {

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotThrowForDeprecatedMethodWhenApplicationInCorrectPackage() {
      Taikai taikai = Taikai.builder()
          .classes(CorrectPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.springBootApplicationShouldBeIn("com.enofex.taikai.spring")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldThrowForDeprecatedMethodWhenApplicationOutsidePackage() {
      Taikai taikai = Taikai.builder()
          .classes(WrongPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.springBootApplicationShouldBeIn("com.example.other")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ApplicationClassShouldResideInPackageZeroArg {

    @Test
    void shouldThrowNPEWhenApplicationClassResideInPackageCalledWithoutNamespace() {
      assertThrows(NullPointerException.class, () -> Taikai.builder()
          .classes(CorrectPackageApplication.class)
          .spring(spring -> spring.boot(BootConfigurer::applicationClassShouldResideInPackage))
          .build());
    }

    @Test
    void shouldDelegateToTwoArgMethodWhenNamespaceIsSet() {
      assertThrows(AssertionError.class, () -> Taikai.builder()
          .namespace("com.enofex.taikai.spring")
          .spring(spring -> spring.boot(BootConfigurer::applicationClassShouldResideInPackage))
          .build()
          .check());
    }
  }

  @Nested
  class ApplicationClassShouldResideInPackageWithConfiguration {

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotThrowForDeprecatedMethodWithConfigurationWhenApplicationInCorrectPackage() {
      Taikai taikai = Taikai.builder()
          .classes(CorrectPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.springBootApplicationShouldBeIn("com.enofex.taikai.spring",
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenApplicationClassInCorrectPackageWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(CorrectPackageApplication.class)
          .spring(spring -> spring.boot(
              boot -> boot.applicationClassShouldResideInPackage("com.enofex.taikai.spring",
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

  }

  @Nested
  class Disable {

    @Test
    void shouldDisableBootConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(WrongPackageApplication.class)
          .spring(spring -> spring.boot(boot -> {
            boot.applicationClassShouldResideInPackage("com.example.other");
            boot.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @SpringBootApplication
  static class CorrectPackageApplication {

  }

  @SpringBootApplication
  static class WrongPackageApplication {

  }
}
