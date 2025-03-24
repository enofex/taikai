package com.enofex.taikai;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

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
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.FailureReport;
import com.tngtech.archunit.lang.Priority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Taikai {

  private final boolean failOnEmpty;
  private final String namespace;
  private final JavaClasses classes;
  private final Collection<String> excludedClasses;
  private final Collection<TaikaiRule> rules;

  private Taikai(Builder builder) {
    this.failOnEmpty = builder.failOnEmpty;
    this.namespace = builder.namespace;
    this.classes = builder.classes;
    this.excludedClasses = requireNonNullElse(builder.excludedClasses, emptyList());
    this.rules = Stream.concat(
            builder.configurers.all().stream().flatMap(configurer -> configurer.rules().stream()),
            builder.rules.stream())
        .toList();

    if (this.namespace != null && this.classes != null) {
      throw new IllegalArgumentException("Setting namespace and classes are not supported");
    }

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
    return this.classes;
  }

  public Collection<String> excludedClasses() {
    return this.excludedClasses;
  }

  public Collection<TaikaiRule> rules() {
    return this.rules;
  }

  /**
   * Executes all rules and fails immediately on the first violation.
   *
   * <p>Each rule is checked sequentially, and if a rule fails, an exception
   * is thrown immediately, stopping further execution.</p>
   */
  public void check() {
    this.rules.forEach(rule -> rule.check(this.namespace, this.classes, this.excludedClasses));
  }

  /**
   * Executes all rules and collects all violations before failing.
   *
   * <p>Unlike {@link #check()}, this method evaluates all rules and
   * aggregates all failures into a single report. If violations exist, an {@link AssertionError} is
   * thrown with a detailed failure summary.</p>
   *
   * @throws AssertionError if any rule violations are found.
   */
  public void checkAll() {
    EvaluationResult result = new EvaluationResult(() -> "All Taikai rules", Priority.MEDIUM);

    for (TaikaiRule rule : this.rules) {
      result.add(rule.archRule()
          .evaluate(rule.javaClasses(this.namespace, this.classes, this.excludedClasses)));
    }

    FailureReport report = result.getFailureReport();

    if (!report.isEmpty()) {
      throw new AssertionError(report.toString());
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static final class Builder {

    private final Configurers configurers;
    private final Collection<TaikaiRule> rules;
    private final Collection<String> excludedClasses;
    private boolean failOnEmpty;
    private String namespace;
    private JavaClasses classes;

    public Builder() {
      this.configurers = new Configurers();
      this.rules = new ArrayList<>();
      this.excludedClasses = new ArrayList<>();
    }

    public Builder(Taikai taikai) {
      this.configurers = new Configurers();
      this.rules = taikai.rules();
      this.excludedClasses = taikai.excludedClasses();
      this.failOnEmpty = taikai.failOnEmpty();
      this.namespace = taikai.namespace();
      this.classes = taikai.classes();
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

    public Builder classes(JavaClasses classes) {
      this.classes = classes;
      return this;
    }

    public Builder excludeClasses(Collection<String> classNames) {
      this.excludedClasses.addAll(classNames);
      return this;
    }

    public Builder excludeClasses(String... classNames) {
      this.excludedClasses.addAll(Arrays.asList(classNames));
      return this;
    }

    public Builder java(Customizer<JavaConfigurer.Disableable> customizer) {
      return configure(customizer, JavaConfigurer.Disableable::new);
    }

    public Builder logging(Customizer<LoggingConfigurer.Disableable> customizer) {
      return configure(customizer, LoggingConfigurer.Disableable::new);
    }

    public Builder test(Customizer<TestConfigurer.Disableable> customizer) {
      return configure(customizer, TestConfigurer.Disableable::new);
    }

    public Builder spring(Customizer<SpringConfigurer.Disableable> customizer) {
      return configure(customizer, SpringConfigurer.Disableable::new);
    }

    private <T extends Configurer> Builder configure(Customizer<T> customizer,
        Function<ConfigurerContext, T> supplier) {
      requireNonNull(customizer);
      requireNonNull(supplier);

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
