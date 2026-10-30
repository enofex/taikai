package com.enofex.taikai.exports.spi;

import com.enofex.taikai.TaikaiRule;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The project that a {@link TaikaiExport} exports.
 */
public interface ExportContext {

  /**
   * Returns the class of the export that receives this context.
   *
   * @return the class of the export
   */
  Class<? extends TaikaiExport> export();

  /**
   * Returns the project directory, the working directory of the test run, which Maven and Gradle
   * set to the directory of the module under test. The paths in the settings of an export are
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
   * Returns the configured rules, in the order they run.
   *
   * @return the rules
   */
  Collection<TaikaiRule> rules();

  /**
   * Returns the class loader of the test run, for example to find resources or classpath entries
   * with {@link ClassLoader#getResources(String)}.
   *
   * @return the class loader
   */
  ClassLoader classLoader();

  /**
   * Returns the configuration of the export that receives this context, for example its settings.
   * The configuration is registered per export with
   * {@code discoverExports(exports -> exports.configure(MyExport.class, ...))}, so every export
   * only sees its own configuration.
   *
   * <pre>{@code
   * String file = context.configuration(MyExport.Settings.class)
   *     .map(MyExport.Settings::file)
   *     .orElse("rules.txt");
   * }</pre>
   *
   * @param type the type of the configuration, or a supertype of the configured object
   * @param <T> the type of the configuration
   * @return the configuration, or an empty optional if the export is not configured
   * @throws IllegalStateException if the export is configured with an object of another type
   */
  default <T> Optional<T> configuration(Class<T> type) {
    return Optional.empty();
  }

  /**
   * Returns the configuration of the export that receives this context, for exports that cannot
   * run without one.
   *
   * @param type the type of the configuration, or a supertype of the configured object
   * @param <T> the type of the configuration
   * @return the configuration
   * @throws IllegalStateException if the export is not configured, or configured with an object
   *     of another type
   * @see #configuration(Class)
   */
  default <T> T requireConfiguration(Class<T> type) {
    return configuration(type).orElseThrow(() -> new IllegalStateException(
        ("Export %s is not configured with %s, add it with "
            + "discoverExports(exports -> exports.configure(%s.class, ...))").formatted(
            export().getName(), type.getName(), export().getSimpleName())));
  }
}
