package com.enofex.taikai;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class PerCheckContext implements CheckContext {

  private final Taikai taikai;
  private final Path projectDirectory;
  private final Class<? extends TaikaiCheck> check;
  private final @Nullable Object configuration;

  PerCheckContext(Taikai taikai, Path projectDirectory, Class<? extends TaikaiCheck> check,
      @Nullable Object configuration) {
    this.taikai = requireNonNull(taikai);
    this.projectDirectory = requireNonNull(projectDirectory);
    this.check = requireNonNull(check);
    this.configuration = configuration;
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
    return this.taikai.namespace();
  }

  @Override
  public JavaClasses classes() {
    return TaikaiRule.javaClasses(defaultConfiguration(), this.taikai.namespace(),
        this.taikai.classes(), this.taikai.excludedClasses(), this.taikai.excludedCriteria());
  }

  @Override
  public ClassLoader classLoader() {
    return Taikai.classLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    requireNonNull(type);

    if (this.configuration == null) {
      return Optional.empty();
    }

    if (!type.isInstance(this.configuration)) {
      throw new IllegalStateException("Check %s is configured with %s, not with %s".formatted(
          this.check.getName(), this.configuration.getClass().getName(), type.getName()));
    }

    return Optional.of(type.cast(this.configuration));
  }
}
