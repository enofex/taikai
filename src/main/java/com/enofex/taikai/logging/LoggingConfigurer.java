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
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Collection;
import java.util.List;

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
 *             .classesShouldUseLogger("org.slf4j.Logger", ".*Service")
 *             .loggersShouldFollowConventions("org.slf4j.Logger", ".*", List.of(JavaModifier.PRIVATE, JavaModifier.STATIC))
 *     );
 * }</pre>
 */
public class LoggingConfigurer extends AbstractConfigurer {

  LoggingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that classes matching the given regex should contain a field of the specified logger type.
   *
   * @param typeName the fully qualified logger type (e.g., {@code org.slf4j.Logger})
   * @param regex the regex for class names expected to use the logger
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex) {
    return classesShouldUseLogger(typeName, regex, defaultConfiguration());
  }

  /**
   * Adds a rule that classes matching the given regex should contain a field of the specified logger type.
   *
   * @param clazz the logger type (e.g., {@code org.slf4j.Logger.class})
   * @param regex the regex for class names expected to use the logger
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex) {
    return classesShouldUseLogger(clazz.getName(), regex, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldUseLogger(Class, String)}, but with {@link Configuration} for customization.
   *
   * @param clazz the logger type
   * @param regex the regex for class names expected to use the logger
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex,
      Configuration configuration) {
    return classesShouldUseLogger(clazz.getName(), regex, configuration);
  }

  /**
   * See {@link #classesShouldUseLogger(String, String)}, but with {@link Configuration} for customization.
   *
   * @param typeName the fully qualified logger type
   * @param regex the regex for class names expected to use the logger
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(haveFieldOfType(typeName))
            .as("Classes with names matching %s should use a logger of type %s".formatted(regex,
                typeName)),
        configuration));
  }

  /**
   * Adds a rule that ensures all logger fields follow naming and modifier conventions.
   * Uses an empty list of required modifiers by default.
   *
   * @param typeName the fully qualified logger type (e.g., {@code org.slf4j.Logger})
   * @param regex the regex pattern for class names
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex) {
    return loggersShouldFollowConventions(typeName, regex, List.of(),
        defaultConfiguration());
  }

  /**
   * See {@link #loggersShouldFollowConventions(String, String)}, but with {@link Configuration} for customization.
   *
   * @param typeName the fully qualified logger type
   * @param regex the regex pattern for class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
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
   * @param regex the regex pattern for class names
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex) {
    return loggersShouldFollowConventions(clazz.getName(), regex, List.of(),
        defaultConfiguration());
  }

  /**
   * See {@link #loggersShouldFollowConventions(Class, String)}, but with {@link Configuration} for customization.
   *
   * @param clazz the logger type
   * @param regex the regex pattern for class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
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
   * @param regex the regex pattern for class names
   * @param requiredModifiers a collection of required {@link JavaModifier}s
   * @return this configurer instance for fluent chaining
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
   * @param regex the regex pattern for class names
   * @param requiredModifiers a collection of required {@link JavaModifier}s
   * @return this configurer instance for fluent chaining
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
   * @param regex the regex pattern for class names
   * @param requiredModifiers a collection of required {@link JavaModifier}s
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
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
   * @param regex the regex pattern for class names
   * @param requiredModifiers a collection of required {@link JavaModifier}s
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should(followLoggerConventions(typeName, regex, requiredModifiers))
            .as("Loggers in classes matching %s should follow conventions and be of type %s with required modifiers %s".formatted(
                regex, typeName, requiredModifiers)),
        configuration));
  }

  public static final class Disableable extends LoggingConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public LoggingConfigurer disable() {
      disable(LoggingConfigurer.class);
      return this;
    }
  }
}
