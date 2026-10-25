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
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class ServiceRegistrationsCheck implements TaikaiCheck {

  private static final List<String> SERVICE_DIRECTORIES = List.of(
      "src/main/resources/META-INF/services", "src/test/resources/META-INF/services");

  @Override
  public String description() {
    return "Service registrations should refer to valid providers";
  }

  @Override
  public List<String> violations(CheckContext context) {
    List<String> violations = new ArrayList<>();

    for (String directory : SERVICE_DIRECTORIES) {
      for (Path registration : registrations(context.projectDirectory().resolve(directory))) {
        violations.addAll(violations(context.projectDirectory().relativize(registration),
            registration, context.classLoader()));
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
}
