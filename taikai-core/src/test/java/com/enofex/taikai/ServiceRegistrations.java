package com.enofex.taikai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Builds a {@link Taikai} that discovers only the given implementations of a service, like a
 * library that registers them in {@code META-INF/services} on the classpath.
 */
final class ServiceRegistrations {

  private ServiceRegistrations() {
  }

  static Taikai build(Taikai.Builder builder, Class<?> service, Path directory,
      Class<?>... implementations) {
    return build(builder, service, directory,
        Arrays.stream(implementations).map(Class::getName).toArray(String[]::new));
  }

  /**
   * Builds with a context class loader whose registration file of the service lists only the
   * given class names, written to a temporary file in the given directory.
   */
  static Taikai build(Taikai.Builder builder, Class<?> service, Path directory,
      String... classNames) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      Path registration = Files.writeString(Files.createTempFile(directory, "services", ""),
          String.join(System.lineSeparator(), classNames));
      Thread.currentThread().setContextClassLoader(new RegisteredClassLoader(contextClassLoader,
          "META-INF/services/" + service.getName(), registration.toUri().toURL()));

      return builder.build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  /**
   * Returns the classes of the given service implementations, in their order.
   */
  static List<Class<?>> classes(Iterable<?> implementations) {
    List<Class<?>> classes = new ArrayList<>();
    implementations.forEach(implementation -> classes.add(implementation.getClass()));
    return classes;
  }

  private static final class RegisteredClassLoader extends ClassLoader {

    private final String resource;
    private final URL registration;

    RegisteredClassLoader(ClassLoader parent, String resource, URL registration) {
      super(parent);
      this.resource = resource;
      this.registration = registration;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      return this.resource.equals(name)
          ? Collections.enumeration(List.of(this.registration))
          : super.getResources(name);
    }
  }
}
