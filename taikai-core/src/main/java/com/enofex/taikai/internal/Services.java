package com.enofex.taikai.internal;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Discovers the implementations of a service registered in {@code META-INF/services}, such as
 * the checks and the exports, and hands them their configuration.
 */
public final class Services {

  private Services() {
  }

  /**
   * Returns the class loader that discovers the services: the context class loader of the current
   * thread, or else the one that loaded Taikai.
   *
   * @return the class loader
   */
  public static ClassLoader classLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader != null ? classLoader : Services.class.getClassLoader();
  }

  /**
   * Instantiates the registered implementations of the service, except the excluded ones, sorted
   * by their description. Before any is instantiated, it fails if an excluded or configured
   * implementation is not registered, or if a configured implementation is excluded.
   *
   * @param service the service
   * @param kind the name of an implementation in messages, such as {@code "check"}
   * @param excluded the classes of the implementations not to instantiate
   * @param configured the classes of the implementations that are configured
   * @param description the description of an implementation
   * @param failure creates the exception to throw from a message and its cause, if any
   * @param <T> the type of the service
   * @return the implementations
   */
  public static <T> List<T> discover(Class<T> service, String kind,
      Collection<? extends Class<?>> excluded, Collection<? extends Class<?>> configured,
      Function<T, String> description,
      BiFunction<String, @Nullable Throwable, RuntimeException> failure) {
    String registrationFile = "META-INF/services/" + service.getName();

    try {
      List<ServiceLoader.Provider<T>> providers =
          ServiceLoader.load(service, classLoader()).stream().toList();

      validate(kind, registrationFile, providers.stream()
          .map(ServiceLoader.Provider::type)
          .collect(Collectors.toSet()), excluded, configured, failure);

      return providers.stream()
          .filter(provider -> !excluded.contains(provider.type()))
          .map(ServiceLoader.Provider::get)
          .sorted(Comparator.comparing(description))
          .toList();
    } catch (ServiceConfigurationError | LinkageError e) {
      throw failure.apply("Could not discover the %ss registered in %s"
          .formatted(kind, registrationFile), e);
    }
  }

  /**
   * Returns the configuration of an implementation as the given type.
   *
   * @param kind the name of an implementation in messages, such as {@code "check"}
   * @param implementation the class of the implementation
   * @param configuration the configuration of the implementation, or {@code null}
   * @param type the type of the configuration
   * @param <T> the type of the configuration
   * @return the configuration, or empty if the implementation is not configured
   * @throws IllegalStateException if the configuration is not of the given type
   */
  public static <T> Optional<T> configuration(String kind, Class<?> implementation,
      @Nullable Object configuration, Class<T> type) {
    requireNonNull(type);

    if (configuration == null) {
      return Optional.empty();
    }

    if (!type.isInstance(configuration)) {
      throw new IllegalStateException("%s %s is configured with %s, not with %s".formatted(
          capitalized(kind), implementation.getName(), configuration.getClass().getName(),
          type.getName()));
    }

    return Optional.of(type.cast(configuration));
  }

  private static void validate(String kind, String registrationFile,
      Set<Class<?>> registered, Collection<? extends Class<?>> excluded,
      Collection<? extends Class<?>> configured,
      BiFunction<String, @Nullable Throwable, RuntimeException> failure) {
    String name = capitalized(kind);

    for (Class<?> excludedClass : excluded) {
      if (!registered.contains(excludedClass)) {
        throw failure.apply("%s %s is excluded, but not registered in %s"
            .formatted(name, excludedClass.getName(), registrationFile), null);
      }
    }

    for (Class<?> configuredClass : configured) {
      if (excluded.contains(configuredClass)) {
        throw failure.apply("%s %s is configured, but excluded"
            .formatted(name, configuredClass.getName()), null);
      }

      if (!registered.contains(configuredClass)) {
        throw failure.apply("%s %s is configured, but not registered in %s"
            .formatted(name, configuredClass.getName(), registrationFile), null);
      }
    }
  }

  private static String capitalized(String kind) {
    return kind.substring(0, 1).toUpperCase(Locale.ROOT) + kind.substring(1);
  }
}
