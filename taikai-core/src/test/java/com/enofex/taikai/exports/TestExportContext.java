package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

final class TestExportContext implements ExportContext {

  private final Class<? extends TaikaiExport> export;
  private final Path projectDirectory;
  private final Supplier<@Nullable String> namespace;
  private final @Nullable JavaClasses classes;
  private final @Nullable Object configuration;

  private TestExportContext(Class<? extends TaikaiExport> export, Path projectDirectory,
      Supplier<@Nullable String> namespace, @Nullable JavaClasses classes,
      @Nullable Object configuration) {
    this.export = export;
    this.projectDirectory = projectDirectory;
    this.namespace = namespace;
    this.classes = classes;
    this.configuration = configuration;
  }

  static TestExportContext of(Class<? extends TaikaiExport> export, Path projectDirectory) {
    return new TestExportContext(export, projectDirectory, () -> null, null, null);
  }

  TestExportContext withNamespace(Supplier<@Nullable String> namespace) {
    return new TestExportContext(this.export, this.projectDirectory, namespace, this.classes,
        this.configuration);
  }

  TestExportContext withClasses(JavaClasses classes) {
    return new TestExportContext(this.export, this.projectDirectory, this.namespace, classes,
        this.configuration);
  }

  TestExportContext withConfiguration(Object configuration) {
    return new TestExportContext(this.export, this.projectDirectory, this.namespace, this.classes,
        configuration);
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
    return this.namespace.get();
  }

  @Override
  public JavaClasses classes() {
    return requireNonNull(this.classes);
  }

  @Override
  public Collection<TaikaiRule> rules() {
    return List.of();
  }

  @Override
  public EvaluationResult evaluate(TaikaiRule rule) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClassLoader classLoader() {
    return TestExportContext.class.getClassLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    return Optional.ofNullable(this.configuration).map(type::cast);
  }
}
