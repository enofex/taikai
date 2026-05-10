package com.enofex.taikai.logging;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class LoggingConfigurerTest {

  @Test
  void shouldApplyLoggerConventionsWithClass() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithTypeName() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                List.of(PRIVATE, FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  /**
   * Violations in base classes should not be reported, as the base class may be part
   * of an external library, which is not under control of the developer.
   */
  @Test
  void shouldApplyLoggerConventionsEvenIfLoggerInBaseClassDoesNotFollowConventions() {
    Taikai taikai = Taikai.builder()
        .classes(SubClassNotViolatingConventions.class)
        .logging(logging -> logging
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                List.of(PRIVATE, FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowLoggerConventionsWithClassNaming() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsNotFollowedNaming.class)
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowLoggerConventionsWithClassModifier() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsPartiallyModifier.class)
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

  private static class SubClassNotViolatingConventions extends LoggerConventionsNotFollowedNaming {
  }

  @Test
  void shouldApplyClassesShouldUseLoggerWithTypeName() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.classesShouldUseLogger("java.util.logging.Logger", ".*LoggerConventionsFollowed"))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenClassShouldUseLoggerButDoesNot() {
    Taikai taikai = Taikai.builder()
        .classes(NoLoggerClass.class)
        .logging(logging -> logging.classesShouldUseLogger("java.util.logging.Logger", ".*NoLoggerClass"))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldApplyClassesShouldUseLoggerWithClass() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.classesShouldUseLogger(Logger.class, ".*LoggerConventionsFollowed"))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithClassWithoutModifiers() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger"))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithTypeNameWithoutModifiers() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.loggersShouldFollowConventions("java.util.logging.Logger", "logger"))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyClassesShouldUseLoggerWithClassAndConfiguration() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.classesShouldUseLogger(Logger.class,
            ".*LoggerConventionsFollowed",
            com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithTypeNameAndConfiguration() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithClassAndConfiguration() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger",
                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldApplyLoggerConventionsWithClassAndModifiersAndConfiguration() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger",
                List.of(PRIVATE, FINAL),
                com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldDisableLoggingConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(NoLoggerClass.class)
        .logging(logging -> {
          logging.classesShouldUseLogger("java.util.logging.Logger", ".*NoLoggerClass");
          logging.disable();
        })
        .build();

    assertDoesNotThrow(taikai::check);
  }

  private static class NoLoggerClass {
    private String name = "hello";
  }
}
