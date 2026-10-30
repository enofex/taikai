package com.enofex.taikai;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class PerExportContext implements ExportContext {

  private final Taikai taikai;
  private final Path projectDirectory;
  private final Class<? extends TaikaiExport> export;
  private final @Nullable Object configuration;

  PerExportContext(Taikai taikai, Path projectDirectory, Class<? extends TaikaiExport> export,
      @Nullable Object configuration) {
    this.taikai = requireNonNull(taikai);
    this.projectDirectory = requireNonNull(projectDirectory);
    this.export = requireNonNull(export);
    this.configuration = configuration;
  }

  @Override
  public Class<? extends TaikaiExport> export() {
    return this.export;
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
  public Collection<TaikaiRule> rules() {
    return this.taikai.rules();
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
      throw new IllegalStateException("Export %s is configured with %s, not with %s".formatted(
          this.export.getName(), this.configuration.getClass().getName(), type.getName()));
    }

    return Optional.of(type.cast(this.configuration));
  }
}
