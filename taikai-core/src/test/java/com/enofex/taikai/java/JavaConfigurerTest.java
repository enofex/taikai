package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.java.JavaFixtures.NonFinalUtilityClass;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JavaConfigurerTest {

  @Nested
  class Disable {

    @Test
    void shouldDisableJavaConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(NonFinalUtilityClass.class)
          .java(java -> java
              .utilityClassesShouldBeFinalAndHavePrivateConstructor()
              .disable())
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }
}
