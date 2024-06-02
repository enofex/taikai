package com.enfoex.taikai;

import com.enfoex.taikai.java.JavaConfigurer;
import com.enfoex.taikai.logging.LoggingConfigurer;
import com.enfoex.taikai.spring.SpringConfigurer;
import com.enfoex.taikai.junit5.JUnit5Configurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Collection;
import java.util.Objects;

public final class Taikai {

  private boolean failOnEmpty;
  private String namespace;
  private final Collection<ArchRule> rules;

  private Taikai(Builder builder) {
    this.failOnEmpty = builder.failOnEmpty;
    this.namespace = builder.namespace;
    this.rules = builder.configurers.all()
        .stream()
        .flatMap(configurer -> configurer.rules().stream())
        .toList();

    ArchConfiguration.get()
        .setProperty("archRule.failOnEmptyShould", Boolean.valueOf(this.failOnEmpty).toString());
  }

  public boolean failOnEmpty() {
    return this.failOnEmpty;
  }

  public String namespace() {
    return this.namespace;
  }

  public JavaClasses classes() {
    return Namespace.classes(this.namespace);
  }

  public JavaClasses testClasses() {
    return Namespace.testClasses(this.namespace);
  }

  public Collection<ArchRule> rules() {
    return this.rules;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private final Configurers configurers;
    private boolean failOnEmpty;
    private String namespace;

    public Builder() {
      this.configurers = new Configurers();
    }

    public Builder failOnEmpty(boolean failOnEmpty) {
      this.failOnEmpty = failOnEmpty;
      return this;
    }

    public Builder namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder java(Customizer<JavaConfigurer> customizer) {
      Objects.requireNonNull(customizer);
      customizer.customize(this.configurers.getOrApply(
          new JavaConfigurer(new ConfigurerContext(this.configurers))));
      return this;
    }

    public Builder spring(Customizer<SpringConfigurer> customizer) {
      Objects.requireNonNull(customizer);
      customizer.customize(this.configurers.getOrApply(
          new SpringConfigurer(new ConfigurerContext(this.configurers))));
      return this;
    }

    public Builder logging(Customizer<LoggingConfigurer> customizer) {
      Objects.requireNonNull(customizer);
      customizer.customize(this.configurers.getOrApply(
          new LoggingConfigurer(new ConfigurerContext(this.configurers))));
      return this;
    }

    public Builder junit5(Customizer<JUnit5Configurer> customizer) {
      Objects.requireNonNull(customizer);
      customizer.customize(this.configurers.getOrApply(
          new JUnit5Configurer(new ConfigurerContext(this.configurers))));
      return this;
    }

    public Taikai build() {
      return new Taikai(this);
    }
  }
}
