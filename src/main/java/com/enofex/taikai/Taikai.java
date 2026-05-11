package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.logging.LoggingConfigurer;
import com.enofex.taikai.quarkus.QuarkusConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.FailureReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Central entry point for defining and executing Taikai architectural rules.
 *
 * <p>This class coordinates {@link TaikaiRule} definitions, manages configuration
 * through various {@code Configurer} implementations (Java, Spring, Logging, Test), and executes
 * rules against the imported {@link JavaClasses} for a given namespace.</p>
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
  @Nullable
  private final String namespace;
  @Nullable
  private final JavaClasses classes;
  private final Collection<String> excludedClasses;
  private final Collection<TaikaiRule> rules;

  private Taikai(Builder builder) {
    this.failOnEmpty = builder.failOnEmpty;
    this.namespace = builder.namespace;
    this.classes = builder.classes;
    this.excludedClasses = builder.excludedClasses;
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
  public @Nullable String namespace() {
    return this.namespace;
  }

  /**
   * Returns the {@link JavaClasses} directly provided for rule evaluation.
   *
   * @return the imported Java classes, or {@code null} if {@link #namespace()} is used instead
   */
  public @Nullable JavaClasses classes() {
    return this.classes;
  }

  /**
   * Returns the list of class exclusion patterns that should not be validated by any rule.
   *
   * <p>The following patterns are supported:
   * <ul>
   *   <li><b>Fully qualified class name</b><br>
   *       {@code com.enofex.taikai.internal.a.MyClass}</li>
   *
   *   <li><b>Package wildcard</b> (classes directly in a package)<br>
   *       {@code com.enofex.taikai.internal.a.*}</li>
   *
   *   <li><b>Recursive package wildcard</b> (package and all subpackages)<br>
   *       {@code com.enofex.taikai.internal.a..}</li>
   * </ul>
   *
   * <p>Patterns are matched against the fully qualified class name
   * (e.g. {@code com.enofex.taikai.internal.a.MyClass}).</p>
   *
   * @return collection of class exclusion patterns
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
    StringBuilder report = new StringBuilder();
    int allViolations = 0;
    int rulesViolated = 0;

    for (TaikaiRule rule : this.rules) {
      FailureReport ruleReport = rule
          .archRule()
          .evaluate(rule.javaClasses(this.namespace, this.classes, this.excludedClasses))
          .getFailureReport();

      if (!ruleReport.isEmpty()) {
        rulesViolated++;

        report.append(System.lineSeparator())
            .append("Rule: ")
            .append(rule.archRule().getDescription())
            .append(System.lineSeparator());

        for (String detail : ruleReport.getDetails()) {
          allViolations++;

          report.append("\t")
              .append(detail)
              .append(System.lineSeparator());
        }
      }
    }

    if (allViolations > 0) {
      throw new AssertionError(String.format("Found %d Taikai violations for %d rules!%n%s",
          allViolations, rulesViolated, report));
    }
  }

  /**
   * Creates a new {@link Builder} instance for fluent configuration of Taikai.
   *
   * @return a new {@link Builder}
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a new {@link Builder} pre-populated with the current configuration of this
   * {@link Taikai} instance. Useful for creating a modified copy of an existing configuration.
   *
   * <pre>{@code
   * Taikai base = Taikai.builder()
   *     .namespace("com.example.project")
   *     .java(java -> java.fieldsShouldNotBePublic())
   *     .build();
   *
   * Taikai extended = base.toBuilder()
   *     .java(java -> java.noUsageOfSystemOutOrErr())
   *     .build();
   * }</pre>
   *
   * @return a new {@link Builder} initialized from this instance
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Builder for constructing a {@link Taikai} instance with namespace, rules, and configurers.
   *
   * <p>Use {@link Taikai#builder()} to obtain a new instance. Chain configurer methods
   * ({@link #java}, {@link #spring}, {@link #test}, etc.) to register architectural rules,
   * then call {@link #build()} to produce a {@link Taikai} ready for {@link Taikai#check()} or
   * {@link Taikai#checkAll()}.</p>
   *
   * <h2>Example</h2>
   * <pre>{@code
   * Taikai.builder()
   *     .namespace("com.example.project")
   *     .failOnEmpty(true)
   *     .java(java -> java
   *         .noUsageOfDeprecatedAPIs()
   *         .fieldsShouldNotBePublic())
   *     .spring(spring -> spring
   *         .noAutowiredFields()
   *         .controllers(ctrl -> ctrl.shouldNotDependOnOtherControllers()))
   *     .build()
   *     .checkAll();
   * }</pre>
   */
  public static final class Builder {

    private final Configurers configurers;
    private final Collection<TaikaiRule> rules;
    private final Collection<String> excludedClasses;
    private boolean failOnEmpty;
    private @Nullable String namespace;
    private @Nullable JavaClasses classes;

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

    /**
     * Registers a single custom {@link TaikaiRule} to be evaluated.
     *
     * @param rule the rule to add
     * @return this builder for fluent chaining
     */
    public Builder addRule(TaikaiRule rule) {
      this.rules.add(rule);
      return this;
    }

    /**
     * Registers a collection of custom {@link TaikaiRule}s to be evaluated.
     *
     * @param rules the rules to add
     * @return this builder for fluent chaining
     */
    public Builder addRules(Collection<TaikaiRule> rules) {
      this.rules.addAll(rules);
      return this;
    }

    /**
     * Configures whether Taikai should fail when a rule matches no classes.
     * Defaults to {@code false}.
     *
     * @param failOnEmpty {@code true} to fail on empty rule results
     * @return this builder for fluent chaining
     */
    public Builder failOnEmpty(boolean failOnEmpty) {
      this.failOnEmpty = failOnEmpty;
      return this;
    }

    /**
     * Sets the base package namespace to scan for classes. All rules will be applied
     * to classes within this package and its subpackages.
     *
     * <p>Cannot be combined with {@link #classes(JavaClasses)}.</p>
     *
     * @param namespace the root package (e.g., {@code "com.example.project"})
     * @return this builder for fluent chaining
     */
    public Builder namespace(@Nullable String namespace) {
      this.namespace = namespace;
      return this;
    }

    /**
     * Provides a pre-imported set of {@link JavaClasses} to run rules against,
     * instead of scanning by namespace.
     *
     * <p>Cannot be combined with {@link #namespace(String)}.</p>
     *
     * @param classes the classes to check
     * @return this builder for fluent chaining
     */
    public Builder classes(@Nullable JavaClasses classes) {
      this.classes = classes;
      return this;
    }

    /**
     * Imports and uses the specified classes directly as the analysis target,
     * instead of scanning by namespace.
     *
     * <p>Cannot be combined with {@link #namespace(String)}.</p>
     *
     * @param classes the class literals to import and check
     * @return this builder for fluent chaining
     */
    public Builder classes(Class<?>... classes) {
      this.classes = new ClassFileImporter().importClasses(classes);
      return this;
    }

    /**
     * Excludes the specified classes from all rule checks by their class objects.
     *
     * @param classes the classes to exclude
     * @return this builder for fluent chaining
     */
    public Builder excludeClasses(Class<?>... classes) {
      for (Class<?> clazz : classes) {
        this.excludedClasses.add(clazz.getName());
      }
      return this;
    }

    /**
     * Excludes the specified classes from all rule checks by their fully qualified names.
     * Supports wildcard patterns:
     * <ul>
     *   <li>{@code com.example.Foo} — single class</li>
     *   <li>{@code com.example.*} — all classes directly in the package</li>
     *   <li>{@code com.example..} — all classes in the package and subpackages</li>
     * </ul>
     *
     * @param classNames the class names or patterns to exclude
     * @return this builder for fluent chaining
     */
    public Builder excludeClasses(Collection<String> classNames) {
      this.excludedClasses.addAll(classNames);
      return this;
    }

    /**
     * Excludes the specified classes from all rule checks by their fully qualified names.
     * Supports the same wildcard patterns as {@link #excludeClasses(Collection)}.
     *
     * @param classNames the class names or patterns to exclude
     * @return this builder for fluent chaining
     */
    public Builder excludeClasses(String... classNames) {
      this.excludedClasses.addAll(Arrays.asList(classNames));
      return this;
    }

    /**
     * Configures general Java code quality and design rules using the provided {@link Customizer}.
     *
     * @param customizer the customizer for {@link JavaConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder java(Customizer<JavaConfigurer> customizer) {
      return configure(customizer, JavaConfigurer::new);
    }

    /**
     * Configures logging conventions using the provided {@link Customizer}.
     *
     * @param customizer the customizer for {@link LoggingConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder logging(Customizer<LoggingConfigurer> customizer) {
      return configure(customizer, LoggingConfigurer::new);
    }

    /**
     * Configures test class and method rules using the provided {@link Customizer}.
     *
     * @param customizer the customizer for {@link TestConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder test(Customizer<TestConfigurer> customizer) {
      return configure(customizer, TestConfigurer::new);
    }

    /**
     * Configures Spring Framework-specific architectural rules using the provided
     * {@link Customizer}.
     *
     * @param customizer the customizer for {@link SpringConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder spring(Customizer<SpringConfigurer> customizer) {
      return configure(customizer, SpringConfigurer::new);
    }

    /**
     * Configures Quarkus-specific architectural rules using the provided {@link Customizer}.
     *
     * @param customizer the customizer for {@link QuarkusConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder quarkus(Customizer<QuarkusConfigurer> customizer) {
      return configure(customizer, QuarkusConfigurer::new);
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

    /**
     * Builds the configured {@link Taikai} instance.
     *
     * @return a new {@link Taikai} instance ready for {@link Taikai#check()} or
     *         {@link Taikai#checkAll()}
     * @throws IllegalArgumentException if both {@link #namespace} and {@link #classes} are set
     */
    public Taikai build() {
      return new Taikai(this);
    }
  }
}
