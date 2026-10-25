package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Checks the {@link java.util.ServiceLoader} registrations of the project, the files in
 * {@code src/main/resources/META-INF/services} and {@code src/test/resources/META-INF/services}
 * of the {@linkplain CheckContext#projectDirectory() project directory}. Every registered service
 * must exist, and every provider must exist, implement the service, and be a public class with a
 * public constructor without parameters, as the {@code ServiceLoader} requires.
 *
 * <p>The check needs no configuration. A project directory without these directories has nothing
 * to check, so the check passes. When the registrations lie elsewhere, for example in a module
 * while the project directory is the root of a multi-module build, configure the directories
 * instead. A configured directory must exist:</p>
 *
 * <pre>
 * .discoverChecks(checks -&gt; checks
 *     .projectDirectory(Path.of(".."))
 *     .configure(ServiceRegistrationsCheck.class, ServiceRegistrationsCheck.directories(
 *         "my-module/src/test/resources/META-INF/services")))
 * </pre>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class ServiceRegistrationsCheck implements TaikaiCheck {

  private static final List<String> DEFAULT_DIRECTORIES = List.of(
      "src/main/resources/META-INF/services", "src/test/resources/META-INF/services");

  /**
   * Returns the settings that check the registrations in the given directories instead of the
   * default ones. Unlike the default directories, a configured directory must exist.
   *
   * @param directories the paths of the directories with the registrations, relative to the
   *     project directory
   * @return the settings
   */
  public static Settings directories(String... directories) {
    return new Settings(List.of(directories));
  }

  @Override
  public String description() {
    return "Service registrations should refer to valid providers";
  }

  @Override
  public List<String> violations(CheckContext context) {
    Path projectDirectory = context.projectDirectory();
    Optional<Settings> settings = context.configuration(Settings.class);
    List<String> violations = new ArrayList<>();

    for (String directory : settings.map(Settings::directories).orElse(DEFAULT_DIRECTORIES)) {
      Path path = projectDirectory.resolve(directory);

      if (settings.isPresent() && !Files.isDirectory(path)) {
        violations.add("%s is missing in %s".formatted(directory, projectDirectory));
      } else {
        for (Path registration : registrations(path)) {
          violations.addAll(violations(projectDirectory.relativize(registration), registration,
              context.classLoader()));
        }
      }
    }

    return violations;
  }

  private static List<Path> registrations(Path directory) {
    if (!Files.isDirectory(directory)) {
      return List.of();
    }

    try (Stream<Path> files = Files.list(directory)) {
      return files.filter(Files::isRegularFile).sorted().toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not list " + directory, e);
    }
  }

  private static List<String> violations(Path name, Path registration, ClassLoader classLoader) {
    String service = registration.getFileName().toString();
    Optional<Class<?>> serviceType = load(service, classLoader);

    if (serviceType.isEmpty()) {
      return List.of("Service %s registered in %s does not exist".formatted(service, name));
    }

    return providers(registration).stream()
        .flatMap(provider -> violation(serviceType.get(), provider, name, classLoader).stream())
        .toList();
  }

  private static Optional<String> violation(Class<?> service, String provider, Path name,
      ClassLoader classLoader) {
    Optional<Class<?>> providerType = load(provider, classLoader);

    if (providerType.isEmpty()) {
      return Optional.of("Provider %s registered in %s does not exist".formatted(provider, name));
    }

    if (!service.isAssignableFrom(providerType.get())) {
      return Optional.of("Provider %s registered in %s does not implement %s"
          .formatted(provider, name, service.getName()));
    }

    if (!isInstantiable(providerType.get())) {
      return Optional.of(("Provider %s registered in %s is not a public class with a public "
          + "constructor without parameters").formatted(provider, name));
    }

    return Optional.empty();
  }

  private static List<String> providers(Path registration) {
    try {
      return Files.readAllLines(registration, StandardCharsets.UTF_8).stream()
          .map(line -> line.replaceFirst("#.*", "").strip())
          .filter(line -> !line.isEmpty())
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + registration, e);
    }
  }

  private static Optional<Class<?>> load(String className, ClassLoader classLoader) {
    try {
      return Optional.of(Class.forName(className, false, classLoader));
    } catch (ClassNotFoundException | LinkageError e) {
      return Optional.empty();
    }
  }

  private static boolean isInstantiable(Class<?> type) {
    if (!Modifier.isPublic(type.getModifiers()) || Modifier.isAbstract(type.getModifiers())) {
      return false;
    }

    try {
      type.getConstructor();
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * The settings of the {@link ServiceRegistrationsCheck}.
   *
   * @param directories the paths of the directories with the registrations, relative to the
   *     project directory
   */
  public record Settings(List<String> directories) {

    /**
     * Creates the settings.
     *
     * @param directories the paths of the directories with the registrations, relative to the
     *     project directory
     * @throws IllegalArgumentException if no directory is given
     */
    public Settings {
      directories = List.copyOf(directories);

      if (directories.isEmpty()) {
        throw new IllegalArgumentException("At least one directory must be given");
      }
    }
  }
}
