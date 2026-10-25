package com.enofex.taikai.checks;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class TestCheckContext implements CheckContext {

  private final Class<? extends TaikaiCheck> check;
  private final Path projectDirectory;
  private final @Nullable JavaClasses classes;
  private final @Nullable Object configuration;

  private TestCheckContext(Class<? extends TaikaiCheck> check, Path projectDirectory,
      @Nullable JavaClasses classes, @Nullable Object configuration) {
    this.check = check;
    this.projectDirectory = projectDirectory;
    this.classes = classes;
    this.configuration = configuration;
  }

  static TestCheckContext of(Class<? extends TaikaiCheck> check, Path projectDirectory) {
    return new TestCheckContext(check, projectDirectory, null, null);
  }

  TestCheckContext withClasses(JavaClasses classes) {
    return new TestCheckContext(this.check, this.projectDirectory, classes, this.configuration);
  }

  TestCheckContext withConfiguration(Object configuration) {
    return new TestCheckContext(this.check, this.projectDirectory, this.classes, configuration);
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
    return null;
  }

  @Override
  public JavaClasses classes() {
    return requireNonNull(this.classes);
  }

  @Override
  public ClassLoader classLoader() {
    return TestCheckContext.class.getClassLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    return Optional.ofNullable(this.configuration).map(type::cast);
  }
}
