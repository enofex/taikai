package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SpringConfigurerTest {

  @Nested
  class NoAutowiredFields {

    @Test
    void shouldThrowWhenFieldAnnotatedWithAutowired() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAutowiredField.class)
          .spring(SpringConfigurer::noAutowiredFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoFieldAnnotatedWithAutowired() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithConstructorInjection.class)
          .spring(SpringConfigurer::noAutowiredFields)
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableSpringConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAutowiredField.class)
          .spring(spring -> {
            spring.noAutowiredFields();
            spring.disable();
          })
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class ClassWithAutowiredField {

    @Autowired
    private String dependency;
  }

  static class ClassWithConstructorInjection {

    private final String dependency;

    ClassWithConstructorInjection(String dependency) {
      this.dependency = dependency;
    }
  }

  @Test
  void shouldReturnAnnotatedWithValidatedPredicate() {
    assertNotNull(SpringDescribedPredicates.annotatedWithValidated(true));
    assertNotNull(SpringDescribedPredicates.annotatedWithValidated(false));
  }
}
