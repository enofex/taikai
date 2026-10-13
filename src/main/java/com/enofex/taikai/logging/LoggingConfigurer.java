package com.enofex.taikai.logging;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.haveFieldOfType;
import static com.enofex.taikai.logging.LoggerConventions.followLoggerConventions;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configures and enforces logging-related rules using {@link com.tngtech.archunit ArchUnit}
 * through the Taikai framework.
 *
 * <p>This configurer ensures that classes consistently use a defined logger type (e.g., SLF4J)
 * and that logger fields follow naming and modifier conventions.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .logging(logging -> logging
 *             .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), "org.slf4j.Logger")
 *             .loggersShouldFollowConventions("org.slf4j.Logger", ".*", List.of(JavaModifier.PRIVATE, JavaModifier.STATIC))
 *     );
 * }</pre>
 */
public final class LoggingConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public LoggingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that classes matching the given criteria should contain a field of the specified
   * logger type.
   *
   * @param classes the classes expected to use the logger, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param clazz the logger type (e.g., {@code org.slf4j.Logger.class})
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public LoggingConfigurer classesShouldUseLogger(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz) {
    return classesShouldUseLogger(classes, clazz.getName(), defaultConfiguration());
  }

  /**
   * See {@link #classesShouldUseLogger(DescribedPredicate, Class)}, but with {@link Configuration}
   * for customization.
   *
   * @param classes the classes expected to use the logger
   * @param clazz the logger type
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public LoggingConfigurer classesShouldUseLogger(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz, Configuration configuration) {
    return classesShouldUseLogger(classes, clazz.getName(), configuration);
  }

  /**
   * Adds a rule that classes matching the given criteria should contain a field of the specified
   * logger type.
   *
   * @param classes the classes expected to use the logger, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param typeName the fully qualified logger type (e.g., {@code org.slf4j.Logger})
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public LoggingConfigurer classesShouldUseLogger(DescribedPredicate<? super JavaClass> classes,
      String typeName) {
    return classesShouldUseLogger(classes, typeName, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldUseLogger(DescribedPredicate, String)}, but with {@link Configuration}
   * for customization.
   *
   * @param classes the classes expected to use the logger
   * @param typeName the fully qualified logger type
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public LoggingConfigurer classesShouldUseLogger(DescribedPredicate<? super JavaClass> classes,
      String typeName, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(haveFieldOfType(typeName))
            .as("Classes %s should use a logger of type %s".formatted(classes.getDescription(),
                typeName)),
        configuration));
  }

  /**
   * Adds a rule that ensures all logger fields follow naming and modifier conventions.
   * Uses an empty list of required modifiers by default.
   *
   * @param typeName the fully qualified logger type (e.g., {@code org.slf4j.Logger})
   * @param regex the regex that logger field names must match
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex) {
    return loggersShouldFollowConventions(typeName, regex, List.of(),
        defaultConfiguration());
  }

  /**
   * See {@link #loggersShouldFollowConventions(String, String)}, but with {@link Configuration} for customization.
   *
   * @param typeName the fully qualified logger type
   * @param regex the regex that logger field names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Configuration configuration) {
    return loggersShouldFollowConventions(typeName, regex, List.of(),
        configuration);
  }

  /**
   * Adds a rule that ensures all logger fields follow naming and modifier conventions.
   * Uses an empty list of required modifiers by default.
   *
   * @param clazz the logger type (e.g., {@code org.slf4j.Logger.class})
   * @param regex the regex that logger field names must match
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex) {
    return loggersShouldFollowConventions(clazz.getName(), regex, List.of(),
        defaultConfiguration());
  }

  /**
   * See {@link #loggersShouldFollowConventions(Class, String)}, but with {@link Configuration} for customization.
   *
   * @param clazz the logger type
   * @param regex the regex that logger field names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Configuration configuration) {
    return loggersShouldFollowConventions(clazz.getName(), regex, List.of(),
        configuration);
  }

  /**
   * Adds a rule that ensures all logger fields follow naming and modifier conventions,
   * requiring specific modifiers.
   *
   * @param typeName the fully qualified logger type (e.g., {@code org.slf4j.Logger})
   * @param regex the regex that logger field names must match
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(typeName, regex, requiredModifiers,
        defaultConfiguration());
  }

  /**
   * Adds a rule that ensures all logger fields follow naming and modifier conventions,
   * requiring specific modifiers.
   *
   * @param clazz the logger type (e.g., {@code org.slf4j.Logger.class})
   * @param regex the regex that logger field names must match
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Collection<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(clazz.getName(), regex, requiredModifiers,
        defaultConfiguration());
  }

  /**
   * See {@link #loggersShouldFollowConventions(Class, String, Collection)}, but with {@link Configuration} for customization.
   *
   * @param clazz the logger type
   * @param regex the regex that logger field names must match
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return loggersShouldFollowConventions(clazz.getName(), regex, requiredModifiers,
        configuration);
  }

  /**
   * See {@link #loggersShouldFollowConventions(String, String, Collection)}, but with {@link Configuration} for customization.
   *
   * @param typeName the fully qualified logger type
   * @param regex the regex that logger field names must match
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should(followLoggerConventions(typeName, regex, requiredModifiers))
            .as(requiredModifiers.isEmpty()
                ? "Loggers of type %s should have names matching %s".formatted(typeName, regex)
                : "Loggers of type %s should have names matching %s and all of these modifiers %s"
                    .formatted(typeName, regex, requiredModifiers.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")))),
        configuration));
  }

  @Override
  public LoggingConfigurer disable() {
    disable(LoggingConfigurer.class);

    return this;
  }
}
