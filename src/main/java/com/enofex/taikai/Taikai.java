package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.jpa.JpaConfigurer;
import com.enofex.taikai.logging.LoggingConfigurer;
import com.enofex.taikai.quarkus.QuarkusConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.FailureReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;

/**
 * Central entry point for defining and executing Taikai architectural rules.
 *
 * <p>This class coordinates {@link TaikaiRule} definitions, manages configuration
 * through various {@code Configurer} implementations (Java, Spring, Logging, Test), and executes
 * rules against the imported {@link JavaClasses} for a given namespace. Together with the rules,
 * it runs {@link TaikaiCheck}s, which check the project beyond its classes.</p>
 *
 * <p>Rules can be executed in three modes:
 * <ul>
 *   <li>{@link #check()} – stops at the first failure</li>
 *   <li>{@link #checkAll()} – evaluates all rules and aggregates failures</li>
 *   <li>{@link #dynamicTests()} – runs every rule as a separate JUnit test</li>
 * </ul>
 *
 * <p>Before the rules run, each mode logs a warning through {@link System.Logger} for a namespace
 * that contains no classes to check, such as a misspelled one, and for an exclusion that matches no
 * class. Neither fails the rules.</p>
 *
 * <p>Use the {@link Builder} to configure and create an instance.</p>
 *
 * @see TaikaiRule
 * @see TaikaiCheck
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
  private final Collection<DescribedPredicate<? super JavaClass>> excludedCriteria;
  private final Collection<TaikaiRule> rules;
  private final @Nullable ChecksConfigurer checksConfigurer;
  private final List<ConfiguredCheck> checks;

  private Taikai(Builder builder) {
    this.failOnEmpty = builder.failOnEmpty;
    this.namespace = builder.namespace;
    this.classes = builder.classes;
    this.excludedClasses = List.copyOf(builder.excludedClasses);
    this.excludedCriteria = List.copyOf(builder.excludedCriteria);
    this.rules = Stream.concat(
            builder.configurers.all().stream().flatMap(configurer -> configurer.rules().stream()),
            builder.rules.stream())
        .toList();
    this.checksConfigurer = builder.checksConfigurer != null
        ? new ChecksConfigurer(builder.checksConfigurer)
        : null;

    if (this.namespace != null && this.classes != null) {
      throw new IllegalArgumentException("Setting namespace and classes are not supported");
    }

    ArchConfiguration.get()
        .setProperty("archRule.failOnEmptyShould", Boolean.toString(this.failOnEmpty));

    this.checks = this.checksConfigurer != null
        ? configuredChecks(this.checksConfigurer)
        : List.of();
  }

  private List<ConfiguredCheck> configuredChecks(ChecksConfigurer configurer) {
    return discoveredChecks(configurer).stream()
        .map(check -> new ConfiguredCheck(check, configurer.decorate(new PerCheckContext(this,
            configurer.projectDirectory(), check.getClass(),
            configurer.configuration(check.getClass())))))
        .toList();
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
   * Returns the criteria selecting classes that should not be validated by any rule.
   *
   * @return collection of class exclusion criteria
   */
  public Collection<DescribedPredicate<? super JavaClass>> excludedCriteria() {
    return this.excludedCriteria;
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
   * Returns all active {@link TaikaiCheck} instances that will be executed after the rules: the
   * discovered checks without the excluded ones, sorted by their description.
   *
   * @return collection of active checks
   * @see Builder#discoverChecks(Consumer)
   */
  public Collection<TaikaiCheck> checks() {
    return this.checks.stream()
        .map(ConfiguredCheck::check)
        .toList();
  }

  /**
   * Executes all configured rules and checks and fails immediately on the first violation.
   *
   * <p>Each rule is evaluated sequentially, followed by each check. If a rule or check fails,
   * execution stops and an {@link AssertionError} is thrown with the failure details.</p>
   *
   * @throws AssertionError if any rule or check fails
   */
  public void check() {
    logWarnings();

    this.rules.forEach(rule -> rule.check(this.namespace, this.classes, this.excludedClasses,
        this.excludedCriteria));

    this.checks.forEach(Taikai::assertNoViolations);
  }

  /**
   * Executes all rules and checks and collects all violations before failing.
   *
   * <p>Unlike {@link #check()}, this method evaluates all rules and checks and
   * aggregates all failures into a single report. If violations exist, an {@link AssertionError} is
   * thrown with a detailed failure summary.</p>
   *
   * @throws AssertionError if any rule or check violations are found.
   */
  public void checkAll() {
    logWarnings();

    StringBuilder report = new StringBuilder();
    int allViolations = 0;
    int rulesViolated = 0;
    int checksViolated = 0;

    for (TaikaiRule rule : this.rules) {
      FailureReport ruleReport = rule
          .archRule()
          .evaluate(rule.javaClasses(this.namespace, this.classes, this.excludedClasses,
              this.excludedCriteria))
          .getFailureReport();

      if (!ruleReport.isEmpty()) {
        rulesViolated++;
        allViolations += appendViolations(report, "Rule", rule.archRule().getDescription(),
            ruleReport.getDetails());
      }
    }

    for (ConfiguredCheck check : this.checks) {
      List<String> violations = violations(check);

      if (!violations.isEmpty()) {
        checksViolated++;
        allViolations += appendViolations(report, "Check", check.check().description(),
            violations);
      }
    }

    if (allViolations > 0) {
      throw new AssertionError(String.format("Found %d Taikai violations for %s!%n%s",
          allViolations, violated(rulesViolated, checksViolated), report));
    }
  }

  private static String violated(int rulesViolated, int checksViolated) {
    if (checksViolated == 0) {
      return rulesViolated + " rules";
    }

    return rulesViolated == 0
        ? checksViolated + " checks"
        : "%d rules and %d checks".formatted(rulesViolated, checksViolated);
  }

  private static int appendViolations(StringBuilder report, String kind, String description,
      List<String> violations) {
    report.append(System.lineSeparator())
        .append(kind)
        .append(": ")
        .append(description)
        .append(System.lineSeparator());

    for (String violation : violations) {
      report.append("\t")
          .append(violation)
          .append(System.lineSeparator());
    }

    return violations.size();
  }

  private static void assertNoViolations(ConfiguredCheck check) {
    List<String> violations = violations(check);

    if (!violations.isEmpty()) {
      throw new AssertionError("Check '%s' was violated (%d times):%n%s".formatted(
          check.check().description(), violations.size(),
          String.join(System.lineSeparator(), violations)));
    }
  }

  private static List<String> violations(ConfiguredCheck check) {
    try {
      return List.copyOf(check.check().violations(check.context()));
    } catch (RuntimeException | AssertionError | LinkageError e) {
      return List.of("Check failed with " + e);
    }
  }

  private static List<TaikaiCheck> discoveredChecks(ChecksConfigurer configurer) {
    try {
      List<ServiceLoader.Provider<TaikaiCheck>> providers =
          ServiceLoader.load(TaikaiCheck.class, classLoader()).stream().toList();
      Collection<Class<?>> excludedChecks = configurer.excludedChecks();

      configurer.validate(providers.stream()
          .map(ServiceLoader.Provider::type)
          .collect(Collectors.toSet()));

      return providers.stream()
          .filter(provider -> !excludedChecks.contains(provider.type()))
          .map(ServiceLoader.Provider::get)
          .sorted(Comparator.comparing(TaikaiCheck::description))
          .toList();
    } catch (ServiceConfigurationError | LinkageError e) {
      throw new TaikaiException("Could not discover the checks registered in "
          + ChecksConfigurer.REGISTRATION_FILE, e);
    }
  }

  private void logWarnings() {
    List<String> warnings = ConfigurationWarnings.of(this);

    if (!warnings.isEmpty()) {
      System.Logger logger = System.getLogger(Taikai.class.getName());
      warnings.forEach(warning -> logger.log(System.Logger.Level.WARNING, warning));
    }
  }

  static ClassLoader classLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader != null ? classLoader : Taikai.class.getClassLoader();
  }

  /**
   * Returns every configured rule and check as a separate JUnit Jupiter {@link DynamicTest}, named
   * after the rule's or check's description. Each rule passes or fails on its own in the IDE and in
   * test reports, and a single rule can be rerun.
   *
   * <pre>{@code
   * @TestFactory
   * Stream<DynamicTest> architecture() {
   *   return Taikai.builder()
   *       .namespace("com.example.project")
   *       .java(java -> java
   *           .noUsageOfSystemOutOrErr()
   *           .fieldsShouldNotBePublic())
   *       .build()
   *       .dynamicTests();
   * }
   * }</pre>
   *
   * <p>Requires {@code org.junit.jupiter:junit-jupiter-api} on the test classpath.</p>
   *
   * @return one dynamic test per rule, in the order the rules were configured, followed by one
   *     dynamic test per check
   */
  public Stream<DynamicTest> dynamicTests() {
    logWarnings();

    return Stream.concat(
        this.rules.stream()
            .map(rule -> DynamicTest.dynamicTest(rule.archRule().getDescription(),
                () -> rule.check(this.namespace, this.classes, this.excludedClasses,
                    this.excludedCriteria))),
        this.checks.stream()
            .map(check -> DynamicTest.dynamicTest(check.check().description(),
                () -> assertNoViolations(check))));
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

  private record ConfiguredCheck(TaikaiCheck check, CheckContext context) {

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
    private final Collection<DescribedPredicate<? super JavaClass>> excludedCriteria;
    private @Nullable ChecksConfigurer checksConfigurer;
    private boolean failOnEmpty;
    private @Nullable String namespace;
    private @Nullable JavaClasses classes;

    private Builder() {
      this.configurers = new Configurers();
      this.rules = new ArrayList<>();
      this.excludedClasses = new ArrayList<>();
      this.excludedCriteria = new ArrayList<>();
    }

    private Builder(Taikai taikai) {
      this.configurers = new Configurers();
      this.rules = new ArrayList<>(taikai.rules());
      this.excludedClasses = new ArrayList<>(taikai.excludedClasses());
      this.excludedCriteria = new ArrayList<>(taikai.excludedCriteria());
      this.checksConfigurer = taikai.checksConfigurer != null
          ? new ChecksConfigurer(taikai.checksConfigurer)
          : null;
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
     * Runs the {@link TaikaiCheck}s that are registered in
     * {@code META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} on the classpath after
     * the rules, with the default settings. Same as {@code discoverChecks(checks -> {})}.
     *
     * @return this builder for fluent chaining
     * @see #discoverChecks(Consumer)
     */
    public Builder discoverChecks() {
      return discoverChecks(checks -> {
      });
    }

    /**
     * Runs the {@link TaikaiCheck}s that are registered in
     * {@code META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} on the classpath after
     * the rules, configured with the given {@link ChecksConfigurer}. The checks are discovered when
     * {@link #build()} is called and run sorted by their description.
     *
     * <p>Calling this method again customizes the same {@link ChecksConfigurer}, so the settings
     * of all calls add up.</p>
     *
     * <pre>{@code
     * .discoverChecks(checks -> checks
     *     .projectDirectory(Path.of(".."))
     *     .exclude(LicenseCheck.class))
     * }</pre>
     *
     * @param customizer the customizer for {@link ChecksConfigurer}
     * @return this builder for fluent chaining
     * @see java.util.ServiceLoader
     */
    public Builder discoverChecks(Consumer<ChecksConfigurer> customizer) {
      requireNonNull(customizer);

      if (this.checksConfigurer == null) {
        this.checksConfigurer = new ChecksConfigurer();
      }

      customizer.accept(this.checksConfigurer);
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
     * Excludes all classes matching the given criteria from all rule checks, for example
     * generated code: {@code excludeClasses(ClassCriteria.inPackage("..generated.."))}.
     * Can be called multiple times; a class matching any of the criteria is excluded.
     *
     * <p>Like the name-based exclusions, criteria are only applied to classes imported via
     * {@link #namespace(String)}. Classes supplied via {@link #classes(JavaClasses)} or a
     * per-rule {@link TaikaiRule.Configuration} are used as given.</p>
     *
     * @param classes the classes to exclude, selected with {@link ClassCriteria} or an ArchUnit
     *     predicate
     * @return this builder for fluent chaining
     * @see com.enofex.taikai.ClassCriteria
     * @see com.tngtech.archunit.base.DescribedPredicate
     */
    public Builder excludeClasses(DescribedPredicate<? super JavaClass> classes) {
      this.excludedCriteria.add(requireNonNull(classes));
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

    /**
     * Configures Jakarta Persistence (JPA) architectural rules using the provided
     * {@link Customizer}.
     *
     * @param customizer the customizer for {@link JpaConfigurer}
     * @return this builder for fluent chaining
     */
    public Builder jpa(Customizer<JpaConfigurer> customizer) {
      return configure(customizer, JpaConfigurer::new);
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
     * @throws TaikaiException if a registered check cannot be loaded or instantiated, if an
     *     excluded check is not registered, or if a configured check is not registered or is
     *     excluded
     */
    public Taikai build() {
      return new Taikai(this);
    }
  }
}
