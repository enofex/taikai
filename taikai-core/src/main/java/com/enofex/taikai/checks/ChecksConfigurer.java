package com.enofex.taikai.checks;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.ForwardingCheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.core.TaikaiException;
import com.enofex.taikai.internal.Services;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.Nullable;

/**
 * Configures how the {@link TaikaiCheck}s discovered with
 * {@code Taikai.Builder.discoverChecks(Consumer)} are run.
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .discoverChecks(checks -> checks
 *         .exclude(LicenseCheck.class)
 *         .configure(ForbiddenDependenciesCheck.class,
 *             new ForbiddenDependencies(List.of("log4j:log4j")))
 *         .context(context -> new ForwardingCheckContext(context) {
 *           @Override
 *           public ClassLoader classLoader() {
 *             return myClassLoader;
 *           }
 *         }))
 *     .build()
 *     .checkAll();
 * }</pre>
 *
 * @see TaikaiCheck
 */
public final class ChecksConfigurer {

  private final Collection<Class<?>> excludedChecks;
  private final Map<Class<?>, Object> configurations;
  private final Collection<ContextFunction> contextFunctions;

  /**
   * Creates a configurer that runs all discovered checks without configuration.
   */
  public ChecksConfigurer() {
    this.excludedChecks = new ArrayList<>();
    this.configurations = new LinkedHashMap<>();
    this.contextFunctions = new ArrayList<>();
  }

  /**
   * Creates a copy of the given configurer, so that changing one does not change the other.
   *
   * @param configurer the configurer to copy
   */
  public ChecksConfigurer(ChecksConfigurer configurer) {
    this.excludedChecks = new ArrayList<>(configurer.excludedChecks);
    this.configurations = new LinkedHashMap<>(configurer.configurations);
    this.contextFunctions = new ArrayList<>(configurer.contextFunctions);
  }

  /**
   * Excludes the discovered checks of the given classes. An excluded check is not instantiated.
   * {@code Taikai.Builder.build()} fails if a check is not registered.
   *
   * @param checks the classes of the checks to exclude
   * @return this configurer for fluent chaining
   */
  @SafeVarargs
  public final ChecksConfigurer exclude(Class<? extends TaikaiCheck>... checks) {
    this.excludedChecks.addAll(List.of(checks));
    return this;
  }

  /**
   * Configures a check, for example with its settings. The check gets the configuration with
   * {@link CheckContext#configuration(Class)}; other checks do not see it. Configuring a
   * check again replaces its configuration.
   *
   * <pre>{@code
   * .configure(ForbiddenDependenciesCheck.class,
   *     new ForbiddenDependencies(List.of("log4j:log4j")))
   * }</pre>
   *
   * <p>{@code Taikai.Builder.build()} fails if the check is not registered or
   * is excluded.</p>
   *
   * @param check the class of the check
   * @param configuration the configuration of the check
   * @param <C> the type of the check
   * @return this configurer for fluent chaining
   */
  public <C extends TaikaiCheck> ChecksConfigurer configure(Class<C> check, Object configuration) {
    this.configurations.put(requireNonNull(check), requireNonNull(configuration));
    return this;
  }

  /**
   * Replaces the {@link CheckContext} of every check. The function receives the context of
   * a check, including its configuration, and returns the context the check gets instead, usually
   * a {@link ForwardingCheckContext} that overrides single methods.
   *
   * <p>The function is applied once per check and {@code Taikai} instance. All
   * functions, including those for single checks of {@link #context(Class, UnaryOperator)}, are
   * applied in the order they were configured, each receiving the result of the previous one.</p>
   *
   * @param context the function that returns the context to use
   * @return this configurer for fluent chaining
   */
  public ChecksConfigurer context(UnaryOperator<CheckContext> context) {
    this.contextFunctions.add(new ContextFunction(null, requireNonNull(context)));
    return this;
  }

  /**
   * Replaces the {@link CheckContext} of a single check, for example to give it another
   * project directory. The function receives the result of the functions configured before it,
   * and returns the context the check gets instead; other checks skip it.
   *
   * <pre>{@code
   * .context(PomCheck.class, context -> new ForwardingCheckContext(context) {
   *   @Override
   *   public Path projectDirectory() {
   *     return super.projectDirectory().resolve("parent");
   *   }
   * })
   * }</pre>
   *
   * <p>{@code Taikai.Builder.build()} fails if the check is not registered or
   * is excluded.</p>
   *
   * @param check the class of the check
   * @param context the function that returns the context to use
   * @param <C> the type of the check
   * @return this configurer for fluent chaining
   */
  public <C extends TaikaiCheck> ChecksConfigurer context(Class<C> check,
      UnaryOperator<CheckContext> context) {
    this.contextFunctions.add(new ContextFunction(requireNonNull(check), requireNonNull(context)));
    return this;
  }

  /**
   * Discovers the registered checks, except the excluded ones, each with its context.
   * {@code Taikai.Builder.build()} calls it, so it is rarely needed elsewhere.
   *
   * @param projectDirectory the directory of the project
   * @param namespace the namespace of the builder, or {@code null}
   * @param classes supplies the classes of the builder, without the excluded ones
   * @return the checks, each with its context, sorted by their description
   * @throws TaikaiException if the checks cannot be discovered, if an excluded or configured
   *     check is not registered or if a configured check is excluded
   */
  public List<Map.Entry<TaikaiCheck, CheckContext>> discover(Path projectDirectory,
      @Nullable String namespace, Supplier<JavaClasses> classes) {
    requireNonNull(projectDirectory);
    requireNonNull(classes);

    Set<Class<?>> configuredChecks = new LinkedHashSet<>(this.configurations.keySet());
    this.contextFunctions.stream()
        .map(ContextFunction::check)
        .filter(Objects::nonNull)
        .forEach(configuredChecks::add);

    List<TaikaiCheck> checks = Services.discover(TaikaiCheck.class, "check", this.excludedChecks,
        configuredChecks, TaikaiCheck::description, (message, cause) -> cause == null
            ? new TaikaiException(message)
            : new TaikaiException(message, cause));

    return checks.stream()
        .map(check -> Map.entry(check, decorate(new PerCheckContext(projectDirectory,
            check.getClass(), this.configurations.get(check.getClass()), namespace, classes))))
        .toList();
  }

  private CheckContext decorate(CheckContext context) {
    Class<?> check = context.check();
    CheckContext decoratedContext = context;

    for (ContextFunction contextFunction : this.contextFunctions) {
      if (contextFunction.appliesTo(check)) {
        decoratedContext = requireNonNull(contextFunction.function().apply(decoratedContext),
            () -> "The context function for check %s must not return null"
                .formatted(check.getName()));
      }
    }

    return decoratedContext;
  }

  private record ContextFunction(@Nullable Class<?> check, UnaryOperator<CheckContext> function) {

    boolean appliesTo(Class<?> check) {
      return this.check == null || this.check == check;
    }
  }
}
