package com.enofex.taikai.logging;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.logging.LoggingChecks.assertReportsViolations;
import static com.enofex.taikai.logging.LoggingChecks.overload;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.TaikaiRule.Configuration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggingConfigurerTest {

  private static final String LOGGER_TYPE = "java.util.logging.Logger";
  private static final DescribedPredicate<JavaClass> THIS_PACKAGE =
      ClassCriteria.inPackage("com.enofex.taikai.logging");

  @Nested
  class ClassesShouldUseLogger {

    @Test
    void shouldNotThrowWhenSelectedClassesUseLoggerClass() {
      Taikai taikai = Taikai.builder()
          .classes(LoggingService.class, SilentHelper.class)
          .logging(logging -> logging.classesShouldUseLogger(
              ClassCriteria.nameMatching(".*Service"), Logger.class))
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
          .logging(logging -> logging.classesShouldUseLogger(
              ClassCriteria.nameMatching(".*Service"), Logger.class,
              Configuration.of(new ClassFileImporter().importClasses(LoggingService.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<LoggingConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("classesShouldUseLogger(DescribedPredicate, Class)",
              logging -> logging.classesShouldUseLogger(THIS_PACKAGE, Logger.class),
              LoggerConventionsFollowed.class, NoLoggerClass.class),
          overload("classesShouldUseLogger(DescribedPredicate, String)",
              logging -> logging.classesShouldUseLogger(THIS_PACKAGE, LOGGER_TYPE),
              LoggerConventionsFollowed.class, NoLoggerClass.class),
          overload("classesShouldUseLogger(DescribedPredicate, Class, Configuration)",
              logging -> logging.classesShouldUseLogger(THIS_PACKAGE, Logger.class,
                  defaultConfiguration()),
              LoggerConventionsFollowed.class, NoLoggerClass.class),
          overload("classesShouldUseLogger(DescribedPredicate, String, Configuration)",
              logging -> logging.classesShouldUseLogger(THIS_PACKAGE, LOGGER_TYPE,
                  defaultConfiguration()),
              LoggerConventionsFollowed.class, NoLoggerClass.class));
    }
  }

  @Nested
  class LoggersShouldFollowConventions {

    /**
     * Violations in base classes should not be reported, as the base class may be part
     * of an external library, which is not under the control of the developer.
     */
    @Test
    void shouldApplyLoggerConventionsEvenIfLoggerInBaseClassDoesNotFollowConventions() {
      Taikai taikai = Taikai.builder()
          .classes(SubClassNotViolatingConventions.class)
          .logging(logging -> logging
              .loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowLoggerConventionsWithClassModifier() {
      Taikai taikai = Taikai.builder()
          .classes(LoggerConventionsPartiallyModifier.class)
          .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
              List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldDescribeLoggerConventionsByLoggerFieldName() {
      Taikai taikai = Taikai.builder()
          .classes(LoggerConventionsFollowed.class)
          .logging(logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
              List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      assertEquals("Loggers of type java.util.logging.Logger should have names matching logger "
              + "and all of these modifiers PRIVATE, FINAL",
          taikai.rules().iterator().next().archRule().getDescription());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<LoggingConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("loggersShouldFollowConventions(Class, String)",
              logging -> logging.loggersShouldFollowConventions(Logger.class, "logger"),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(String, String)",
              logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger"),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(Class, String, Configuration)",
              logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                  defaultConfiguration()),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(String, String, Configuration)",
              logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                  defaultConfiguration()),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(Class, String, Collection)",
              logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(String, String, Collection)",
              logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(Class, String, Collection, Configuration)",
              logging -> logging.loggersShouldFollowConventions(Logger.class, "logger",
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL), defaultConfiguration()),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class),
          overload("loggersShouldFollowConventions(String, String, Collection, Configuration)",
              logging -> logging.loggersShouldFollowConventions(LOGGER_TYPE, "logger",
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL), defaultConfiguration()),
              LoggerConventionsFollowed.class, LoggerConventionsNotFollowedNaming.class));
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableLoggingConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(NoLoggerClass.class)
          .logging(logging -> {
            logging.classesShouldUseLogger(ClassCriteria.nameMatching(".*NoLoggerClass"),
                LOGGER_TYPE);
            logging.disable();
          })
          .build();

      assertDoesNotThrow(taikai::check);
    }
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

  static class LoggingService {
    private static final Logger LOGGER = Logger.getLogger(LoggingService.class.getName());
  }

  static class SilentService {
  }

  static class SilentHelper {
  }
}
