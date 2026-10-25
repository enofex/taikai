package com.enofex.taikai.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class LoggingCriteriaTest {

  @Test
  void shouldNotThrowWhenSelectedClassesUseLoggerClass() {
    Taikai taikai = Taikai.builder()
        .classes(LoggingService.class, SilentHelper.class)
        .logging(logging -> logging.classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"),
            Logger.class))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenSelectedClassesDoNotUseLoggerTypeName() {
    Taikai taikai = Taikai.builder()
        .classes(LoggingService.class, SilentService.class)
        .logging(logging -> logging.classesShouldUseLogger(
            ClassCriteria.nameMatching(".*Service"), Logger.class.getName()))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldHonorConfiguredClasses() {
    Taikai taikai = Taikai.builder()
        .classes(SilentService.class)
        .logging(logging -> logging.classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"),
            Logger.class,
            Configuration.of(new ClassFileImporter().importClasses(LoggingService.class))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class LoggingService {
    private static final Logger LOGGER = Logger.getLogger(LoggingService.class.getName());
  }

  static class SilentService {
  }

  static class SilentHelper {
  }
}
