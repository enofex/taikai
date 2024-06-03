package com.enfoex.taikai;

import com.enfoex.taikai.java.JavaConfigurer;
import com.enfoex.taikai.logging.LoggingConfigurer;
import com.enfoex.taikai.spring.SpringConfigurer;
import com.enfoex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public final class Taikai {

  private final boolean failOnEmpty;
  private final String namespace;
  private final Collection<TaikaiRule> rules;

  private Taikai(Builder builder) {
    this.failOnEmpty = builder.failOnEmpty;
    this.namespace = builder.namespace;
    this.rules = Stream.concat(
        builder.configurers.all()
            .stream()
            .flatMap(configurer -> configurer.rules().stream()),
        builder.rules.stream()
    ).toList();

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
    return Namespace.withoutTests(this.namespace);
  }

  public JavaClasses classesWithTests() {
    return Namespace.withTests(this.namespace);
  }

  public Collection<TaikaiRule> rules() {
    return this.rules;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void check() {
    this.rules.forEach(rule -> rule.check(this.namespace));
  }

  public static final class Builder {

    private final Configurers configurers;
    private final Collection<TaikaiRule> rules;

    private boolean failOnEmpty;
    private String namespace;

    public Builder() {
      this.configurers = new Configurers();
      this.rules = new ArrayList<>();
    }

    public Builder addRule(TaikaiRule rule) {
      this.rules.add(rule);
      return this;
    }

    public Builder addRules(Collection<TaikaiRule> rules) {
      this.rules.addAll(rules);
      return this;
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

    public Builder test(Customizer<TestConfigurer> customizer) {
      Objects.requireNonNull(customizer);
      customizer.customize(this.configurers.getOrApply(
          new TestConfigurer(new ConfigurerContext(this.configurers))));
      return this;
    }

    public Taikai build() {
      return new Taikai(this);
    }
  }
}
