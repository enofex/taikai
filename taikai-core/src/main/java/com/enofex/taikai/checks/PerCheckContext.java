package com.enofex.taikai.checks;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.internal.Services;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

final class PerCheckContext implements CheckContext {

  private final Path projectDirectory;
  private final Class<? extends TaikaiCheck> check;
  private final @Nullable Object configuration;
  private final @Nullable String namespace;
  private final Supplier<JavaClasses> classes;

  PerCheckContext(Path projectDirectory, Class<? extends TaikaiCheck> check,
      @Nullable Object configuration, @Nullable String namespace, Supplier<JavaClasses> classes) {
    this.projectDirectory = requireNonNull(projectDirectory);
    this.check = requireNonNull(check);
    this.configuration = configuration;
    this.namespace = namespace;
    this.classes = requireNonNull(classes);
  }

  @Override
  public Class<? extends TaikaiCheck> check() {
    return this.check;
  }

  @Override
  public Path projectDirectory() {
    return this.projectDirectory;
  }

  @Override
  public @Nullable String namespace() {
    return this.namespace;
  }

  @Override
  public JavaClasses classes() {
    return this.classes.get();
  }

  @Override
  public ClassLoader classLoader() {
    return Services.classLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    return Services.configuration("check", this.check, this.configuration, type);
  }
}
