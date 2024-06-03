package com.enofex.taikai;

import com.enfoex.taikai.Configurer;
import com.enfoex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  @Test
  void shouldFulfilConstrains() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .spring(spring -> spring
            .configurations(configuration -> configuration
                .shouldHaveNameEndingConfiguration()
                .shouldHaveNameMatching("Action"))
            .controllers(controllers -> controllers
                .shouldBeAnnotatedWithRestController()
                .shouldNotDependOnOtherController()
                .shouldBePackagePrivate())
            .services(Configurer::disable)
            .repositories(Configurer::disable))
        .logging(Configurer::disable)
        .test(Configurer::disable)
        .java(Configurer::disable)
        .build();

    taikai.check();
  }
}