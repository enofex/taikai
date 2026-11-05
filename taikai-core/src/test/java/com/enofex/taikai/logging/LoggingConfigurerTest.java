package com.enofex.taikai.logging;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggingConfigurerTest {

  private static final String LOGGER_TYPE = "java.util.logging.Logger";
  private static final DescribedPredicate<JavaClass> THIS_PACKAGE =
      ClassCriteria.inPackage("com.enofex.taikai.logging");

  @ParameterizedTest(name = "{0}")
  @MethodSource("overloads")
  void shouldReportViolationsWithEveryOverload(Customizer<LoggingConfigurer> rule,
      Class<?> compliant, Class<?> violating) {
    Taikai compliantTaikai = Taikai.builder()
        .classes(compliant)
        .logging(rule)
        .build();
    Taikai violatingTaikai = Taikai.builder()
        .classes(violating)
        .logging(rule)
        .build();

    assertDoesNotThrow(compliantTaikai::check);
    assertThrows(AssertionError.class, violatingTaikai::check);
  }

  static Stream<Arguments> overloads() {
    return Stream.of(
        loggerConventions("loggersShouldFollowConventions(Class, String)",
            logging -> logging.loggersShouldFollowConventions(Logger.class, "logger")),
        loggerConventions("loggersShouldFollowConventions(String, String)",
            logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger")),
        loggerConventions("loggersShouldFollowConventions(Class, String, Configuration)",
            logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                defaultConfiguration())),
        loggerConventions("loggersShouldFollowConventions(String, String, Configuration)",
            logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                defaultConfiguration())),
        loggerConventions("loggersShouldFollowConventions(Class, String, Collection)",
            logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                List.of(PRIVATE, FINAL))),
        loggerConventions("loggersShouldFollowConventions(String, String, Collection)",
            logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                List.of(PRIVATE, FINAL))),
        loggerConventions(
            "loggersShouldFollowConventions(Class, String, Collection, Configuration)",
            logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                List.of(PRIVATE, FINAL), defaultConfiguration())),
        loggerConventions(
            "loggersShouldFollowConventions(String, String, Collection, Configuration)",
            logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                List.of(PRIVATE, FINAL), defaultConfiguration())),
        loggerUsage("classesShouldUseLogger(DescribedPredicate, Class)",
            logging -> logging.classesShouldUseLogger(THIS_PACKAGE, Logger.class)),
        loggerUsage("classesShouldUseLogger(DescribedPredicate, String)",
            logging -> logging.classesShouldUseLogger(THIS_PACKAGE, LOGGER_TYPE)),
        loggerUsage("classesShouldUseLogger(DescribedPredicate, Class, Configuration)",
            logging -> logging.classesShouldUseLogger(THIS_PACKAGE, Logger.class,
                defaultConfiguration())),
        loggerUsage("classesShouldUseLogger(DescribedPredicate, String, Configuration)",
            logging -> logging.classesShouldUseLogger(THIS_PACKAGE, LOGGER_TYPE,
                defaultConfiguration())));
  }

  private static Arguments loggerConventions(String name, Customizer<LoggingConfigurer> rule) {
    return Arguments.of(Named.of(name, rule), LoggerConventionsFollowed.class,
        LoggerConventionsNotFollowedNaming.class);
  }

  private static Arguments loggerUsage(String name, Customizer<LoggingConfigurer> rule) {
    return Arguments.of(Named.of(name, rule), LoggerConventionsFollowed.class,
        NoLoggerClass.class);
  }

  /**
   * Violations in base classes should not be reported, as the base class may be part
   * of an external library, which is not under the control of the developer.
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
  void shouldThrowLoggerConventionsWithClassModifier() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsPartiallyModifier.class)
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldDescribeLoggerConventionsByLoggerFieldName() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerConventionsFollowed.class)
        .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
            List.of(PRIVATE, FINAL)))
        .build();

    assertEquals("Loggers of type java.util.logging.Logger should have names matching logger "
            + "and all of these modifiers PRIVATE, FINAL",
        taikai.rules().iterator().next().archRule().getDescription());
  }

  @Test
  void shouldDisableLoggingConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(NoLoggerClass.class)
        .logging(logging -> {
          logging.classesShouldUseLogger(ClassCriteria.nameMatching(".*NoLoggerClass"),
              "java.util.logging.Logger");
          logging.disable();
        })
        .build();

    assertDoesNotThrow(taikai::check);
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

  private static class NoLoggerClass {
    private String name = "hello";
  }
}
