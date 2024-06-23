package com.enofex.taikai.logging;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.EnumSet;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class LoggingConfigurerTest {

  @Test
  void shouldApplyLoggerConventionsWithClass() {
    TaikaiRule.Configuration configuration = TaikaiRule.Configuration.of(
        new ClassFileImporter().importClasses(LoggerConventionsFollowed.class));

    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", EnumSet.of(PRIVATE, FINAL),
                configuration))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithTypeName() {
    TaikaiRule.Configuration configuration = TaikaiRule.Configuration.of(
        new ClassFileImporter().importClasses(LoggerConventionsFollowed.class));

    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .logging(logging -> logging
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                EnumSet.of(PRIVATE, FINAL), configuration))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowLoggerConventionsWithClassNaming() {
    TaikaiRule.Configuration configuration = TaikaiRule.Configuration.of(
        new ClassFileImporter().importClasses(LoggerConventionsNotFollowedNaming.class));

    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", EnumSet.of(PRIVATE, FINAL),
                configuration))
        .build();

    assertThrows(AssertionError.class, () -> taikai.check());
  }

  @Test
  void shouldThrowLoggerConventionsWithClassModifier() {
    TaikaiRule.Configuration configuration = TaikaiRule.Configuration.of(
        new ClassFileImporter().importClasses(LoggerConventionsPartiallyModifier.class));

    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", EnumSet.of(PRIVATE, FINAL),
                configuration))
        .build();

    assertThrows(AssertionError.class, () -> taikai.check());
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
