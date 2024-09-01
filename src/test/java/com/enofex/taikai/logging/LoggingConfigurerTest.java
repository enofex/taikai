package com.enofex.taikai.logging;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class LoggingConfigurerTest {

  @Test
  void shouldApplyLoggerConventionsWithClass() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(LoggerConventionsFollowed.class))
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithTypeName() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(LoggerConventionsFollowed.class))
        .logging(logging -> logging
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                List.of(PRIVATE, FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowLoggerConventionsWithClassNaming() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(LoggerConventionsNotFollowedNaming.class))
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowLoggerConventionsWithClassModifier() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(LoggerConventionsPartiallyModifier.class))
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  private static class LoggerConventionsFollowed {
    private static final Logger logger = Logger.getLogger(
        LoggerConventionsFollowed.class.getName());
  }

  private static class LoggerConventionsNotFollowedNaming {
    public static Logger LOGGER = Logger.getLogger(
        LoggerConventionsNotFollowedNaming.class.getName());
  }

  private static class LoggerConventionsPartiallyModifier {
    private Logger logger = Logger.getLogger(
        LoggerConventionsPartiallyModifier.class.getName());
  }
}
