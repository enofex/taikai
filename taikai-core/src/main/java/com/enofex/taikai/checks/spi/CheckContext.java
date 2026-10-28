package com.enofex.taikai.checks.spi;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The project that a {@link TaikaiCheck} checks.
 *
 * @see ForwardingCheckContext
 */
public interface CheckContext {

  /**
   * Returns the class of the check that receives this context.
   *
   * @return the class of the check
   */
  Class<? extends TaikaiCheck> check();

  /**
   * Returns the project directory, the working directory of the test run, which Maven and Gradle
   * set to the directory of the module under test. The paths in the settings of a check are
   * relative to it, and may point outside of it with {@code ..}, for example to the root of a
   * multi-module build.
   *
   * @return the absolute project directory
   */
  Path projectDirectory();

  /**
   * Returns the namespace of the configuration.
   *
   * @return the namespace, or {@code null} if classes are configured instead
   */
  @Nullable String namespace();

  /**
   * Returns the classes of the configuration. Classes imported from the namespace exclude test
   * classes and the globally excluded classes; classes set directly with
   * {@code Taikai.Builder#classes} are returned as given, as for the rules. The import of a
   * namespace is cached and shared with the rules and checks.
   *
   * @return the classes
   * @throws RuntimeException a {@code TaikaiException} if the configuration has neither a
   *     namespace nor classes
   */
  JavaClasses classes();

  /**
   * Returns the class loader of the test run, for example to find resources or classpath entries
   * with {@link ClassLoader#getResources(String)}.
   *
   * @return the class loader
   */
  ClassLoader classLoader();

  /**
   * Returns the configuration of the check that receives this context, for example its settings.
   * The configuration is registered per check with
   * {@code discoverChecks(checks -> checks.configure(MyCheck.class, ...))}, so every check only
   * sees its own configuration.
   *
   * <pre>{@code
   * List<String> forbidden = context.configuration(ForbiddenDependencies.class)
   *     .map(ForbiddenDependencies::artifacts)
   *     .orElse(DEFAULT_ARTIFACTS);
   * }</pre>
   *
   * @param type the type of the configuration, or a supertype of the configured object
   * @param <T> the type of the configuration
   * @return the configuration, or an empty optional if the check is not configured
   * @throws IllegalStateException if the check is configured with an object of another type
   */
  default <T> Optional<T> configuration(Class<T> type) {
    return Optional.empty();
  }

  /**
   * Returns the configuration of the check that receives this context, for checks that cannot run
   * without one.
   *
   * @param type the type of the configuration, or a supertype of the configured object
   * @param <T> the type of the configuration
   * @return the configuration
   * @throws IllegalStateException if the check is not configured, or configured with an object of
   *     another type
   * @see #configuration(Class)
   */
  default <T> T requireConfiguration(Class<T> type) {
    return configuration(type).orElseThrow(() -> new IllegalStateException(
        ("Check %s is not configured with %s, add it with "
            + "discoverChecks(checks -> checks.configure(%s.class, ...))").formatted(
            check().getName(), type.getName(), check().getSimpleName())));
  }
}
