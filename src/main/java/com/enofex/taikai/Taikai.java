package com.enofex.taikai;

import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.logging.LoggingConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
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
        .setProperty("archRule.failOnEmptyShould", Boolean.toString(this.failOnEmpty));
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

  public void check() {
    this.rules.forEach(rule -> rule.check(this.namespace));
  }

  public static Builder builder() {
    return new Builder();
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
      return configure(customizer, JavaConfigurer::new);
    }

    public Builder logging(Customizer<LoggingConfigurer> customizer) {
      return configure(customizer, LoggingConfigurer::new);
    }

    public Builder test(Customizer<TestConfigurer> customizer) {
      return configure(customizer, TestConfigurer::new);
    }

    public Builder spring(Customizer<SpringConfigurer> customizer) {
      return configure(customizer, SpringConfigurer::new);
    }

    private <T extends Configurer> Builder configure(Customizer<T> customizer,
        Function<ConfigurerContext, T> supplier) {
      Objects.requireNonNull(customizer);
      Objects.requireNonNull(supplier);

      customizer.customize(this.configurers.getOrApply(supplier.apply(
          new ConfigurerContext(this.namespace, this.configurers)))
      );
      return this;
    }

    public Taikai build() {
      return new Taikai(this);
    }
  }
}
