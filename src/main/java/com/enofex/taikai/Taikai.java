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

/**
 * Central entry point for defining and executing Taikai architectural rules.
 *
 * <p>This class coordinates {@link TaikaiRule} definitions, manages configuration
 * through various {@code Configurer} implementations (Java, Spring, Logging, Test),
 * and executes rules against the imported {@link JavaClasses} for a given namespace.</p>
 *
 * <p>Rules can be executed in two modes:
 * <ul>
 *   <li>{@link #check()} – stops at the first failure</li>
 *   <li>{@link #checkAll()} – evaluates all rules and aggregates failures</li>
 * </ul>
 *
 * <p>Use the {@link Builder} to configure and create an instance.</p>
 *
 * @see TaikaiRule
 * @see Configurer
 * @see Configurers
 * @see Namespace
 */
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

  /**
   * Returns whether Taikai should fail when a rule has no matching elements.
   *
   * @return {@code true} if Taikai should fail on empty rules, {@code false} otherwise
   */
  public boolean failOnEmpty() {
    return this.failOnEmpty;
  }

  /**
   * Returns the namespace configured for rule evaluation.
   *
   * @return the package namespace, or {@code null} if {@link #classes()} is used instead
   */
  public String namespace() {
    return this.namespace;
  }

  /**
   * Returns the {@link JavaClasses} directly provided for rule evaluation.
   *
   * @return the imported Java classes, or {@code null} if {@link #namespace()} is used instead
   */
  public JavaClasses classes() {
    return this.classes;
  }

  /**
   * Returns the list of excluded class names that should not be validated by any rule.
   *
   * @return collection of fully qualified class names to exclude
   */
  public Collection<String> excludedClasses() {
    return this.excludedClasses;
  }

  /**
   * Returns all active {@link TaikaiRule} instances that will be executed.
   *
   * @return collection of active Taikai rules
   */
  public Collection<TaikaiRule> rules() {
    return this.rules;
  }

  /**
   * Executes all configured rules and fails immediately on the first violation.
   *
   * <p>Each rule is evaluated sequentially. If a rule fails, execution stops and
   * an {@link AssertionError} is thrown with the failure details.</p>
   *
   * @throws AssertionError if any rule fails
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
