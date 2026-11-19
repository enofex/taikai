package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.enofex.taikai.internal.Services;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

final class PerExportContext implements ExportContext {

  private final Path projectDirectory;
  private final Class<? extends TaikaiExport> export;
  private final @Nullable Object configuration;
  private final @Nullable String namespace;
  private final Supplier<JavaClasses> classes;
  private final Collection<TaikaiRule> rules;
  private final Function<TaikaiRule, EvaluationResult> evaluate;

  PerExportContext(Path projectDirectory, Class<? extends TaikaiExport> export,
      @Nullable Object configuration, @Nullable String namespace, Supplier<JavaClasses> classes,
      Collection<TaikaiRule> rules, Function<TaikaiRule, EvaluationResult> evaluate) {
    this.projectDirectory = requireNonNull(projectDirectory);
    this.export = requireNonNull(export);
    this.configuration = configuration;
    this.namespace = namespace;
    this.classes = requireNonNull(classes);
    this.rules = requireNonNull(rules);
    this.evaluate = requireNonNull(evaluate);
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
    return this.namespace;
  }

  @Override
  public JavaClasses classes() {
    return this.classes.get();
  }

  @Override
  public Collection<TaikaiRule> rules() {
    return this.rules;
  }

  @Override
  public EvaluationResult evaluate(TaikaiRule rule) {
    return this.evaluate.apply(rule);
  }

  @Override
  public ClassLoader classLoader() {
    return Services.classLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    return Services.configuration("export", this.export, this.configuration, type);
  }
}
